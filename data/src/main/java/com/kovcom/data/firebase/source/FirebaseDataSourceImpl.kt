package com.kovcom.data.firebase.source

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.kovcom.data.model.*
import com.kovcom.data.preferences.LocalDataSource
import com.kovcom.domain.model.GroupType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseDataSourceImpl constructor(
    private val dbInstance: FirebaseFirestore,
    private val localDataSource: LocalDataSource,
    authDataSource: AuthDataSource,
) : FirebaseDataSource, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val currentGroupFlow = MutableStateFlow<String?>(null)

    private val tokenFlow = authDataSource.userFlow.map {
        when (val data = it.data) {
            is UserModelBase.Empty -> {
                Timber.tag(TAG).w("User is empty while getting token")
                null
            }

            is UserModelBase.UserModel -> data.token
            null -> {
                Timber.tag(TAG).w("User is null while getting token")
                null
            }
        }
    }

    override val userGroupsFlow: Flow<Result<List<GroupModel>>> =
        tokenFlow.flatMapLatest { token ->
            if (token == null) {
                Timber.tag(TAG).w("User is null while getting token groups")
                flowOf(Result.success(emptyList<GroupModel>()))
            } else {
                userGroups(token = token)
            }
        }

    override val selectedGroupsFlow: Flow<Result<List<SelectedGroupModel>>> =
        tokenFlow.flatMapLatest { token ->
            if (token == null) {
                Timber.tag(TAG).w("User is null while getting user groups")
                flowOf(Result.success(emptyList()))
            } else {
                selectedGroups
            }
        }

    override val userQuotesFlow =
        currentGroupFlow.combine(tokenFlow) { groupId, token ->
            userQuotesFlow(groupId, token)
        }.flattenMerge(1)

    override val frequenciesFlow = frequenciesFlow()

    override val userFrequencyFlow: Flow<Result<Long>> = tokenFlow.flatMapLatest {
        userFrequenciesFlow(it)
    }

    private val mutex = Mutex()

    override fun subscribeAllGroupsQuotes(groupId: String) {
        launch {
            currentGroupFlow.emit(groupId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun saveNewGroup(group: GroupModel): Result<GroupModel> {
        val token = tokenFlow.first()

        if (token == null) {
            Timber.tag(TAG).w("Token is null while saving new group: ${group.name}")
            return Result.error(Exception("Token is null"))
        }
        return suspendCancellableCoroutine { continuation ->
            val uuid = UUID.randomUUID().toString()
            dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_GROUPS)
                .document(uuid)
                .set(
                    group.copy(
                        id = uuid,
                        quotesCount = 0
                    )
                )
                .addOnSuccessListener {
                    continuation.resume(Result.success(group)) {}
                }
                .addOnFailureListener { exception ->
                    Timber.tag(TAG).e(exception)
                    continuation.resume(Result.error(exception)) {}
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun updateUserFrequency(settingId: Long): Result<Long> {
        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))
        return suspendCancellableCoroutine { continuation ->
            dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .set(hashMapOf(FREQUENCY_FIELD to settingId))
                .addOnSuccessListener {
                    localDataSource.setFrequency(settingId)
                    continuation.resume(Result.success(settingId)) {}
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.error(exception)) {}
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun saveNewQuote(
        groupId: String, quote: QuoteModel,
    ): Result<QuoteModel> {

        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))

        mutex.withLock {
            return suspendCancellableCoroutine { continuation ->
                val uuid = UUID.randomUUID().toString()
                val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
                    .document(token)
                    .collection(COLLECTION_GROUPS)
                    .document(groupId)
                currentDocument.collection(COLLECTION_QUOTES)
                    .document(uuid)
                    .set(quote.apply { id = uuid })
                    .addOnSuccessListener {
                        continuation.resume(Result.success(quote)) {}
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.error(exception)) {}
                    }
                currentDocument.get().addOnCompleteListener {
                    if (!it.result.exists()) {
                        dbInstance.collection(COLLECTION_GROUPS).document(groupId).get()
                            .addOnCompleteListener { snapShot ->
                                val group = snapShot.result.toObject<GroupModel>()
                                group?.let { model ->
                                    currentDocument.set(
                                        model.copy(
                                            quotesCount = (model.quotesCount ?: 0).inc()
                                        )
                                    )
                                }
                            }
                    } else {
                        currentDocument.update(QUOTES_COUNT_FIELD, FieldValue.increment(1))
                    }
                }
            }
        }
    }

    override suspend fun deleteQuote(
        groupId: String,
        quoteId: String,
        isSelected: Boolean,
    ): Result<String> {

        val token = tokenFlow.first()

        if (token == null) {
            Timber.tag(TAG).w("Token is null while deleting quote: $quoteId.")
            return Result.error(Exception("Token is null"))
        }

        return suspendCancellableCoroutine { continuation ->
            val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
                .document()
                .collection(COLLECTION_GROUPS)
                .document(groupId)
            currentDocument
                .collection(COLLECTION_QUOTES)
                .document(quoteId)
                .delete()
                .addOnSuccessListener {
                    currentDocument.update(QUOTES_COUNT_FIELD, FieldValue.increment(-1))
                    if (isSelected) removeQuoteFromSelected(groupId, quoteId, token)
                    continuation.resume(Result.success(quoteId))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.error(exception))
                }
        }
    }

    override suspend fun deleteGroup(groupId: String) {
        val token = tokenFlow.first() ?: return
        val deletePersonalResult = dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_GROUPS)
            .document(groupId)
            .delete()
            .await()

        Timber.tag(TAG).i("Group $groupId deleted successfully:")

        val deleteSelectionResult = dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_SELECTION)
            .document(groupId)
            .delete()
            .await()

        Timber.tag(TAG).i("Group's selection for $groupId deleted successfully")
    }

    override suspend fun selectGroup(groupId: String) {
        currentGroupFlow.emit(groupId)
    }

    override suspend fun saveSelection(
        quote: SelectedQuoteModel,
        groupType: GroupType,
        isSelected: Boolean,
    ): Result<SelectedQuoteModel> {
        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))
        return suspendCancellableCoroutine { continuation ->
            val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_SELECTION)
                .document(quote.groupId)
            currentDocument.get()
                .addOnCompleteListener {
                    if (it.result.exists()) {
                        val current = it.result.toObject<SelectedGroupModel>()
                        val collection = current?.quotesIds.orEmpty().toMutableSet()
                            .let { items ->
                                if (isSelected) {
                                    items.add(SelectedQuoteModelV2(quote.id))
                                } else {
                                    items.firstOrNull { item -> item.quoteId == quote.id }
                                        ?.let {
                                            items.remove(it)
                                        }
                                }
                                items.toList()
                            }
                        Timber.tag(TAG).i("Current group: $current")
                        if (isSelected) {
                            currentDocument.update(SELECTED_QUOTES_ID_FIELD, collection)
                        } else {
                            currentDocument.update(SELECTED_QUOTES_ID_FIELD, collection)
                        }
                        continuation.resume(Result.success(quote)) {}
                    } else {
                        currentDocument.set(
                            SelectedGroupModel(
                                groupId = quote.groupId,
                                quotesIds = listOf(SelectedQuoteModelV2(quote.id)),
                                groupType = groupType,
                            )
                        )
                    }
                }
        }
    }

    data class WidgetGroupInfo(
        val groupId: String,
        val groupType: GroupType,
        val quoteId: String,
        val shownAt: Long,
    )

    override suspend fun getSelectedQuotes(): Result<List<SelectedQuoteModel>> {
        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))

        return suspendCancellableCoroutine { continuation ->
            dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_SELECTION)
                .get()
                .addOnSuccessListener { task ->
                    val groups = mutableListOf<SelectedGroupModel>()
                    for (doc in task.documents) {
                        doc.toObject<SelectedGroupModel>()?.let {
                            groups.add(it)
                        }
                    }
                    val mapped = groups.flatMap { group ->
                        group.quotesIds
                            .map {
                                SelectedQuoteModel(
                                    groupId = group.groupId,
                                    groupType = group.groupType,
                                    id = it.quoteId,
                                    shownAt = it.shownAt
                                )
                            }
                    }
                        .sortedBy { it.shownAt }

                    println("QQQQ: quotes: $mapped")

                    continuation.resume(Result.success(mapped))
                }
        }
    }

    override suspend fun getQuoteById(
        groupId: String,
        groupType: GroupType,
        quoteId: String,
    ): Result<QuoteModel> {
        when (groupType) {
            GroupType.Common -> {
                return suspendCancellableCoroutine { continuation ->
                    dbInstance.collection(COLLECTION_GROUPS)
                        .document(groupId)
                        .collection(COLLECTION_QUOTES)
                        .document(quoteId)
                        .get()
                        .addOnSuccessListener { task ->
                            task.toObject<QuoteModel>()?.let {
                                continuation.resume(Result.success(it))
                            } ?: continuation.resume(Result.error(Exception("Quote is null")))
                        }

                }
            }

            GroupType.Personal -> {
                val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))
                return suspendCancellableCoroutine { continuation ->
                    dbInstance.collection(COLLECTION_PERSONAL)
                        .document(token)
                        .collection(COLLECTION_GROUPS)
                        .document(groupId)
                        .collection(COLLECTION_QUOTES)
                        .document(quoteId)
                        .get()
                        .addOnSuccessListener { task ->
                            task.toObject<QuoteModel>()?.let {
                                continuation.resume(Result.success(it))
                            } ?: continuation.resume(Result.error(Exception("Quote is null")))
                        }
                }

            }
        }
    }

    override suspend fun updateSelectedQuote(
        groupId: String,
        quoteId: String,
        shownTime: Long,
    ) {
        val token = tokenFlow.first() ?: return
        return suspendCancellableCoroutine { continuation ->

            dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_SELECTION)
                .document(groupId)
                .get()
                .addOnSuccessListener { task ->
                    val group = task.toObject<SelectedGroupModel>()
                    val quotes = group?.quotesIds.orEmpty().toMutableSet()
                    quotes.firstOrNull { it.quoteId == quoteId }?.let {
                        quotes.remove(it)
                        quotes.add(it.copy(shownAt = shownTime))
                    }
                    dbInstance.collection(COLLECTION_PERSONAL)
                        .document(token)
                        .collection(COLLECTION_SELECTION)
                        .document(groupId)
                        .update(SELECTED_QUOTES_ID_FIELD, quotes.toList())
                        .addOnSuccessListener {
                            continuation.resume(Unit)
                        }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun editQuote(
        groupId: String,
        quoteId: String,
        editedQuote: String,
        editedAuthor: String,
    ): Result<String> {
        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))
        mutex.withLock {
            return suspendCancellableCoroutine { continuation ->
                val updateMap = hashMapOf<String, String>()
                updateMap[QUOTE_QUOTE_FIELD] = editedQuote
                updateMap[QUOTE_AUTHOR_FIELD] = editedAuthor
                dbInstance.collection(COLLECTION_PERSONAL)
                    .document(token)
                    .collection(COLLECTION_GROUPS)
                    .document(groupId)
                    .collection(COLLECTION_QUOTES)
                    .document(quoteId)
                    .update(updateMap as Map<String, String>)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(quoteId)) {}
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.error(exception)) {}
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun editGroup(
        groupId: String,
        editedName: String,
        editedDescription: String,
    ): Result<String> {
        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))
        return suspendCancellableCoroutine { continuation ->
            val updateMap = hashMapOf<String, String>()
            updateMap[GROUP_NAME_FIELD] = editedName
            updateMap[GROUP_DESCRIPTION_FIELD] = editedDescription
            dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_GROUPS)
                .document(groupId)
                .update(updateMap as Map<String, String>)
                .addOnSuccessListener {
                    continuation.resume(Result.success(groupId)) {}
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.error(exception)) {}
                }
        }
    }

    private fun removeQuoteFromSelected(groupId: String, quoteId: String, token: String) {
        val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_SELECTION)
            .document(groupId)
        currentDocument.collection(COLLECTION_SELECTED_QUOTES)
            .document(quoteId)
            .delete()
            .addOnSuccessListener {
                currentDocument.update(
                    SELECTED_QUOTES_COUNT_FIELD,
                    FieldValue.increment(-1)
                )
            }
    }

    private fun userGroups(token: String): Flow<Result<List<GroupModel>>> = callbackFlow {
        val subscription = dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_GROUPS)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(Result.error(error))
                    return@addSnapshotListener
                }

                value?.let { snapShot ->
                    val groups = mutableListOf<GroupModel>()
                    for (doc in snapShot) {
                        groups.add(doc.toObject())
                    }
                    trySend(Result.success<List<GroupModel>>(groups))
                }
            }
        awaitClose {
            subscription.remove()
            channel.close()
        }
    }

    private val selectedGroups = tokenFlow.flatMapLatest {
        callbackFlow {
            Timber.tag(TAG).i("Selected groups subscription started")
            val subscription = dbInstance.collection(COLLECTION_PERSONAL)
                .document(it.orEmpty())
                .collection(COLLECTION_SELECTION)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Result.error(error))
                        return@addSnapshotListener
                    }

                    value?.let { snapShot ->
                        val groups = mutableListOf<SelectedGroupModel>()
                        for (doc in snapShot) {
                            groups.add(doc.toObject())
                        }
                        Timber.tag(TAG).i("Selected groups: $groups")
                        trySend(Result.success(groups))
                    }
                }
            awaitClose {
                Timber.tag(TAG).i("Selected groups subscription closed")
                subscription.remove()
                channel.close()
            }
        }
    }

    private fun userQuotesFlow(
        groupId: String?,
        token: String?,
    ): Flow<Result<List<QuoteModel>>> {
        if (groupId == null) {
            return flowOf(Result.error(Exception("Group id is null")))
        }
        if (token == null) {
            Timber.tag(TAG).i("Token is null while getting user quotes")
            return flowOf(Result.success(emptyList()))
        }

        return callbackFlow {
            val subscription = dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_GROUPS)
                .document(groupId)
                .collection(COLLECTION_QUOTES)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Result.error(error))
                        return@addSnapshotListener
                    }
                    value?.let { snapShot ->
                        val groups = mutableListOf<QuoteModel>()
                        for (doc in snapShot) {
                            groups.add(doc.toObject())
                        }
                        trySend(Result.success(groups))
                    }
                }
            awaitClose {
                subscription.remove()
                channel.close()
            }
        }
    }

    private fun frequenciesFlow() = callbackFlow {
        val subscription = dbInstance.collection(COLLECTION_FREQUENCY)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(Result.error(error))
                    return@addSnapshotListener
                }
                value?.let { snapShot ->
                    val setting = mutableListOf<FrequencyModel>()
                    for (doc in snapShot) {
                        setting.add(doc.toObject())
                    }
                    trySend(Result.success(setting))
                }
            }
        awaitClose {
            subscription.remove()
            channel.close()
        }
    }

    private fun userFrequenciesFlow(token: String?): Flow<Result<Long>> {
        if (token == null) {
            Timber.tag(TAG).w("Token is null while trying to get user frequencies")
            return flowOf(Result.success(-1))
        }

        return callbackFlow {
            val subscription = dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Result.error(error))
                        return@addSnapshotListener
                    }
                    val frequency =
                        value?.data?.get(FREQUENCY_FIELD) as? Long ?: DEFAULT_FREQUENCY_VALUE
                    localDataSource.setFrequency(frequency)
                    trySend(Result.success(frequency))
                }
            awaitClose {
                subscription.remove()
                channel.close()
            }
        }
    }

    companion object {

        const val TAG = "FirebaseDataSourceImpl"
        const val DEFAULT_FREQUENCY_VALUE = 24L
        private const val COLLECTION_PERSONAL = "personal"
        private const val COLLECTION_SELECTION = "selection"
        private const val COLLECTION_FREQUENCY = "frequency"
        private const val COLLECTION_GROUPS = "groups"
        private const val COLLECTION_QUOTES = "quotes"
        private const val COLLECTION_SELECTED_QUOTES = "selectedquotes"
        private const val FREQUENCY_FIELD = "frequency"
        private const val SHOWN_AT_FIELD = "shownAt"
        private const val SELECTED_QUOTES_COUNT_FIELD = "selectedQuotesCount"
        private const val SELECTED_QUOTES_ID_FIELD = "quotesIds"
        private const val QUOTES_COUNT_FIELD = "quotesCount"
        private const val GROUP_NAME_FIELD = "name"
        private const val GROUP_DESCRIPTION_FIELD = "description"
        private const val QUOTE_QUOTE_FIELD = "quote"
        private const val QUOTE_AUTHOR_FIELD = "author"
    }
}
