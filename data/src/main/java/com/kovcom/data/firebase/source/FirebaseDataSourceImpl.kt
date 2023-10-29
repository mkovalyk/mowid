package com.kovcom.data.firebase.source

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.kovcom.data.model.*
import com.kovcom.data.preferences.LocalDataSource
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

    private val tokenFlow = authDataSource.userFlow.map { it.data?.token }

    override val userGroupsFlow = authDataSource.userFlow.flatMapLatest { token ->
        token.data.let { user ->
            if (user == null) {
                Timber.tag(TAG).w("User is null while getting user groups")
                flowOf(Result.success(emptyList()))
            } else {
                userGroups(token = user.token)
            }
        }
    }

    override val selectedGroupsFlow: Flow<Result<List<SelectedGroupModel>>> =
        authDataSource.userFlow.flatMapLatest { userDataModel ->
            userDataModel.data.let { user ->
                if (user == null) {
                    Timber.tag(TAG).w("User is null while getting user groups")
                    flowOf(Result.success(emptyList()))
                } else {
                    selectedGroups(userId = user.token)
                }
            }
        }

    override val selectedQuotesFlow =
        currentGroupFlow.combine(authDataSource.userFlow) { groupId, token ->
            selectedQuotes(groupId, token.data?.token)
        }.flattenMerge(1)

    override val userQuotesFlow =
        currentGroupFlow.combine(authDataSource.userFlow) { groupId, token ->
            userQuotesFlow(groupId, token.data?.token)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun saveSelection(
        quote: SelectedQuoteModel,
        isSelected: Boolean,
    ): Result<SelectedQuoteModel> {
        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))
        val changed = quote.copy(selectedBy = token)
        return suspendCancellableCoroutine { continuation ->
            val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_SELECTION)
                .document(quote.groupId)
            currentDocument.get().addOnCompleteListener {
                if (it.result.exists()) {
                    val current = it.result.toObject<SelectedGroupModel>()
                    if (isSelected) {
                        currentDocument.update(SELECTED_QUOTES_ID_FIELD,
                                               current?.quoteIds.orEmpty().toMutableList().apply {
                                                   add(quote.id)
                                               })
                        continuation.resume(Result.success(quote)) {}
//                        currentDocument
//                            .collection(COLLECTION_SELECTED_QUOTES)
//                            .document(quote.id)
//                            .set(changed)
//                            .addOnSuccessListener {
//                                currentDocument.update(
//                                    SELECTED_QUOTES_COUNT_FIELD,
//                                    FieldValue.increment(1)
//                                )
//                            }
//                            .addOnFailureListener { exception ->
//                                continuation.resume(ResultDataModel.error(exception)) {}
//                            }
                    } else {
//                        currentDocument
//                            .collection(COLLECTION_SELECTED_QUOTES)
//                            .document(quote.id)
//                            .delete()
                        currentDocument.update(
                            SELECTED_QUOTES_ID_FIELD,
                            current?.quoteIds.orEmpty().toMutableList().apply {
                                add(quote.id)
                            })
                        continuation.resume(Result.success(quote)) {}
                    }
                } else {
                    currentDocument.set(
                        SelectedGroupModel(
                            groupId = quote.groupId,
                            quoteIds = listOf(quote.id),
                        )
                    )
//                        .addOnCompleteListener {
//                        currentDocument
//                            .collection(COLLECTION_SELECTED_QUOTES)
//                            .document(quote.id)
//                            .set(changed)
//                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSelectedQuotes(): Result<List<SelectedQuoteModel>> {
        val token = tokenFlow.first() ?: return Result.error(Exception("Token is null"))

        return suspendCancellableCoroutine { continuation ->
            dbInstance.collectionGroup(COLLECTION_SELECTED_QUOTES)
                .whereEqualTo("selectedBy", token)
                .get()
                .addOnSuccessListener { task ->
                    val quotes = mutableListOf<SelectedQuoteModel>()
                    for (doc in task.documents) {
                        doc.toObject<SelectedQuoteModel>()?.let {
                            quotes.add(it)
                        }
                    }
                    continuation.resume(Result.success(quotes)) {}

                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.error(exception)) {}
                }
        }
    }

    override suspend fun updateSelectedQuote(
        groupId: String,
        quoteId: String,
        shownTime: Long,
    ) {
        val token = tokenFlow.first() ?: return

        dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_SELECTION)
            .document(groupId)
            .collection(COLLECTION_SELECTED_QUOTES)
            .document(quoteId)
            .update(SHOWN_AT_FIELD, shownTime)
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
        mutex.withLock {
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


    private fun userGroups(token: String) = callbackFlow {
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

    private fun selectedGroups(userId: String) = callbackFlow {
        val subscription = dbInstance.collection(COLLECTION_PERSONAL)
            .document(userId)
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

    private fun selectedQuotes(
        groupId: String?,
        token: String?,
    ): Flow<Result<List<SelectedQuoteModel>>> {
        if (groupId == null) {
            return flowOf(Result.error(Exception("Group id is null")))
        }

        if (token == null) {
            Timber.tag(TAG).i("Token is null while getting selected quotes")
            return flowOf(Result.success(emptyList()))
        }

        return callbackFlow {
            val subscription =
                dbInstance.collection(COLLECTION_PERSONAL)
                    .document(token)
                    .collection(COLLECTION_SELECTION)
                    .document(groupId)
                    .collection(COLLECTION_SELECTED_QUOTES)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            trySend(Result.error(error))
                            return@addSnapshotListener
                        }
                        value?.let {
                            val selections = mutableListOf<SelectedQuoteModel>()
                            for (doc in value) {
                                selections.add(doc.toObject())
                            }
                            trySend(
                                Result.success(selections)
                            )
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
                    val frequency = value?.data?.get(FREQUENCY_FIELD) as? Long
                    localDataSource.setFrequency(frequency ?: DEFAULT_FREQUENCY_VALUE)
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
