package com.kovcom.data.firebase.source.impl

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.kovcom.data.firebase.source.AuthDataSource
import com.kovcom.data.firebase.source.FirebaseDataSource
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
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FirebaseDataSourceImpl @Inject constructor(
    private val dbInstance: FirebaseFirestore,
    private val localDataSource: LocalDataSource,
    authDataSource: AuthDataSource,
) : FirebaseDataSource, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val _frequenciesFlow = MutableSharedFlow<ResultDataModel<List<FrequencyDataModel>>>(
        replay = 1
    )

    private val _userFrequencyFlow = MutableSharedFlow<ResultDataModel<Long>>(
        replay = 1
    )

    private val currentGroupFlow = MutableStateFlow<String?>(null)

    override val groupsFlow = groupsFlow()

    private val tokenFlow = authDataSource.userFlow.map { it.data?.token }

    override val userGroupsFlow = authDataSource.userFlow.flatMapLatest { token ->
        token.data.let { user ->
            if (user == null) {
                flowOf(ResultDataModel.error(Exception("User is null")))
            } else {
                userGroups(token = user.token)
            }
        }
    }

    override val selectedGroupsFlow: Flow<ResultDataModel<List<SelectedGroupDataModel>>> = authDataSource.userFlow.flatMapLatest { token ->
        token.data.let { user ->
            if (user == null) {
                flowOf(ResultDataModel.error(Exception("User is null")))
            } else {
                selectedGroups(token = user.token)
            }
        }
    }

    override val selectedQuotesFlow = currentGroupFlow.combine(authDataSource.userFlow) { groupId, token ->
        selectedQuotes(groupId, token.data?.token)
    }.flattenMerge(1)

    override val quotesFlow: Flow<ResultDataModel<List<QuoteDataModel>>> = currentGroupFlow.flatMapConcat { groupId ->
        if (groupId == null) {
            flowOf(ResultDataModel.error(Exception("Group id is null")))
        } else {
            quotesFlow(groupId)
        }
    }

    override val userQuotesFlow = currentGroupFlow.combine(authDataSource.userFlow) { groupId, token ->
        userQuotesFlow(groupId, token.data?.token)
    }.flattenMerge(1)

    override val frequenciesFlow = _frequenciesFlow.asSharedFlow()

    override val userFrequencyFlow = _userFrequencyFlow.asSharedFlow()

    private val mutex = Mutex()

    override fun subscribeAllGroupsQuotes(groupId: String) {
        launch {
            currentGroupFlow.emit(groupId)
        }
    }

    override fun subscribeFrequencySettings() {
        subscribeFrequencies()
        subscribeUserFrequency()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun saveNewGroup(group: GroupDataModel): ResultDataModel<GroupDataModel> {
        val token = tokenFlow.first()

        if (token == null) {
            Timber.tag(TAG).w("Token is null while saving new group: ${group.name}")
            return ResultDataModel.error(Exception("Token is null"))
        }
        return suspendCancellableCoroutine { continuation ->
            val uuid = UUID.randomUUID().toString()
            dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_GROUPS)
                .document(uuid)
                .set(group.apply {
                    id = uuid
                    quotesCount = 0
                })
                .addOnSuccessListener {
                    continuation.resume(ResultDataModel.success(group)) {}
                }
                .addOnFailureListener { exception ->
                    Timber.tag(TAG).e(exception)
                    continuation.resume(ResultDataModel.error(exception)) {}
                }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun updateUserFrequency(settingId: Long): ResultDataModel<Long> =
        suspendCancellableCoroutine { continuation ->
            dbInstance.collection(COLLECTION_PERSONAL)
                .document(localDataSource.token)
                .set(hashMapOf(FREQUENCY_FIELD to settingId))
                .addOnSuccessListener {
                    localDataSource.setFrequency(settingId)
                    continuation.resume(ResultDataModel.success(settingId)) {}
                }
                .addOnFailureListener { exception ->
                    continuation.resume(ResultDataModel.error(exception)) {}
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun saveNewQuote(
        groupId: String, quote: QuoteDataModel,
    ): ResultDataModel<QuoteDataModel> {
        mutex.withLock {
            return suspendCancellableCoroutine { continuation ->
                val uuid = UUID.randomUUID().toString()
                val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
                    .document(localDataSource.token)
                    .collection(COLLECTION_GROUPS)
                    .document(groupId)
                currentDocument.collection(COLLECTION_QUOTES)
                    .document(uuid)
                    .set(quote.apply { id = uuid })
                    .addOnSuccessListener {
                        continuation.resume(ResultDataModel.success(quote)) {}
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(ResultDataModel.error(exception)) {}
                    }
                currentDocument.get().addOnCompleteListener {
                    if (!it.result.exists()) {
                        dbInstance.collection(COLLECTION_GROUPS).document(groupId).get()
                            .addOnCompleteListener { snapShot ->
                                val group = snapShot.result.toObject<GroupDataModel>()
                                group?.let { model ->
                                    currentDocument.set(model.apply {
                                        quotesCount = quotesCount?.inc()
                                    })
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
    ): ResultDataModel<String> {

        val token = tokenFlow.first()

        if (token == null) {
            Timber.tag(TAG).w("Token is null while deleting quote: $quoteId.")
            return ResultDataModel.error(Exception("Token is null"))
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
                    if (isSelected) removeQuoteFromSelected(groupId, quoteId)
                    continuation.resume(ResultDataModel.success(quoteId))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(ResultDataModel.error(exception))
                }
        }
    }

    private fun <T> Task<T>.toResult(): ResultDataModel<T> {
        exception?.let {
            return ResultDataModel.error(it)
        }
        return ResultDataModel.success(this.result)
    }

    override suspend fun deleteGroup(groupId: String) {
        val token = tokenFlow.first()

            ?: return
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun saveSelection(
        quote: SelectedQuoteDataModel,
        isSelected: Boolean,
    ): ResultDataModel<SelectedQuoteDataModel> {
        mutex.withLock {
            return suspendCancellableCoroutine { continuation ->
                val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
                    .document(localDataSource.token)
                    .collection(COLLECTION_SELECTION)
                    .document(quote.groupId)
                currentDocument.get().addOnCompleteListener {
                    if (it.result.exists()) {
                        if (isSelected) {
                            currentDocument
                                .collection(COLLECTION_SELECTED_QUOTES)
                                .document(quote.id)
                                .set(quote.apply { selectedBy = localDataSource.token })
                                .addOnSuccessListener {
                                    continuation.resume(ResultDataModel.success(quote)) {}
                                }
                                .addOnFailureListener { exception ->
                                    continuation.resume(ResultDataModel.error(exception)) {}
                                }
                            currentDocument.update(
                                SELECTED_QUOTES_COUNT_FIELD,
                                FieldValue.increment(1)
                            )
                        } else {
                            currentDocument
                                .collection(COLLECTION_SELECTED_QUOTES)
                                .document(quote.id)
                                .delete()
                            currentDocument.update(
                                SELECTED_QUOTES_COUNT_FIELD,
                                FieldValue.increment(-1)
                            )
                        }
                    } else {
                        currentDocument.set(
                            SelectedGroupDataModel(
                                groupId = quote.groupId,
                                selectedQuotesCount = 1
                            )
                        ).addOnCompleteListener {
                            currentDocument
                                .collection(COLLECTION_SELECTED_QUOTES)
                                .document(quote.id)
                                .set(quote.apply { selectedBy = localDataSource.token })
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSelectedQuotes(): ResultDataModel<List<SelectedQuoteDataModel>> =
        suspendCancellableCoroutine { continuation ->
            dbInstance.collectionGroup(COLLECTION_SELECTED_QUOTES)
                .whereEqualTo("selectedBy", localDataSource.token)
                .get()
                .addOnSuccessListener { task ->
                    val quotes = mutableListOf<SelectedQuoteDataModel>()
                    for (doc in task.documents) {
                        doc.toObject<SelectedQuoteDataModel>()?.let {
                            quotes.add(it)
                        }
                    }
                    continuation.resume(ResultDataModel.success(quotes)) {}

                }
                .addOnFailureListener { exception ->
                    continuation.resume(ResultDataModel.error(exception)) {}
                }
        }

    override suspend fun updateSelectedQuote(
        groupId: String,
        quoteId: String,
        shownTime: Long,
    ) {
        dbInstance.collection(COLLECTION_PERSONAL)
            .document(localDataSource.token)
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
    ): ResultDataModel<String> {
        mutex.withLock {
            return suspendCancellableCoroutine { continuation ->
                val updateMap = hashMapOf<String, String>()
                updateMap[QUOTE_QUOTE_FIELD] = editedQuote
                updateMap[QUOTE_AUTHOR_FIELD] = editedAuthor
                dbInstance.collection(COLLECTION_PERSONAL)
                    .document(localDataSource.token)
                    .collection(COLLECTION_GROUPS)
                    .document(groupId)
                    .collection(COLLECTION_QUOTES)
                    .document(quoteId)
                    .update(updateMap as Map<String, String>)
                    .addOnSuccessListener {
                        continuation.resume(ResultDataModel.success(quoteId)) {}
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(ResultDataModel.error(exception)) {}
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun editGroup(
        groupId: String,
        editedName: String,
        editedDescription: String,
    ): ResultDataModel<String> {
        mutex.withLock {
            return suspendCancellableCoroutine { continuation ->
                val updateMap = hashMapOf<String, String>()
                updateMap[GROUP_NAME_FIELD] = editedName
                updateMap[GROUP_DESCRIPTION_FIELD] = editedDescription
                dbInstance.collection(COLLECTION_PERSONAL)
                    .document(localDataSource.token)
                    .collection(COLLECTION_GROUPS)
                    .document(groupId)
                    .update(updateMap as Map<String, String>)
                    .addOnSuccessListener {
                        continuation.resume(ResultDataModel.success(groupId)) {}
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(ResultDataModel.error(exception)) {}
                    }
            }
        }
    }

    private fun removeQuoteFromSelected(groupId: String, quoteId: String) {
        val currentDocument = dbInstance.collection(COLLECTION_PERSONAL)
            .document(localDataSource.token)
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


    private fun groupsFlow(): Flow<ResultDataModel<List<GroupDataModel>>> =
        callbackFlow {
            val subscription = dbInstance.collection(COLLECTION_GROUPS)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(ResultDataModel.error(error))
                        return@addSnapshotListener
                    }
                    value?.let { snapShot ->
                        val groups = mutableListOf<GroupDataModel>()
                        for (doc in snapShot) {
                            groups.add(doc.toObject())
                        }
                        trySend(ResultDataModel.success(groups))
                    }
                }
            awaitClose { subscription.remove() }
        }


    private fun userGroups(token: String) = callbackFlow {
        val subscription = dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_GROUPS)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(ResultDataModel.error(error))
                    return@addSnapshotListener
                }

                value?.let { snapShot ->
                    val groups = mutableListOf<GroupDataModel>()
                    for (doc in snapShot) {
                        groups.add(doc.toObject())
                    }
                    trySend(ResultDataModel.success(groups))
                }
            }
        awaitClose { subscription.remove() }
    }

    private fun selectedGroups(token: String) = callbackFlow {
        val subscription = dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_SELECTION)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(ResultDataModel.error(error))
                    return@addSnapshotListener
                }

                value?.let { snapShot ->
                    val groups = mutableListOf<SelectedGroupDataModel>()
                    for (doc in snapShot) {
                        groups.add(doc.toObject())
                    }
                    trySend(ResultDataModel.success(groups))
                }
            }
        awaitClose { subscription.remove() }
    }

    private fun quotesFlow(groupId: String): Flow<ResultDataModel<List<QuoteDataModel>>> = callbackFlow {
        val subscription = dbInstance
            .collection(COLLECTION_GROUPS)
            .document(groupId)
            .collection(COLLECTION_QUOTES)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(ResultDataModel.error(error))
                    return@addSnapshotListener
                }
                value?.let { snapShot ->
                    val groups = mutableListOf<QuoteDataModel>()
                    for (doc in snapShot) {
                        groups.add(doc.toObject())
                    }
                    trySend(ResultDataModel.success(groups))
                }
            }
        awaitClose {
            subscription.remove()
        }
    }

    private fun userQuotesFlow(groupId: String?, token: String?): Flow<ResultDataModel<List<QuoteDataModel>>> = callbackFlow {
        if (groupId == null) {
            trySend(ResultDataModel.error(Exception("Group id is null")))
            return@callbackFlow
        }
        if (token == null) {
            trySend(ResultDataModel.error(Exception("Token is null")))
            return@callbackFlow
        }

        val subscription = dbInstance.collection(COLLECTION_PERSONAL)
            .document(token)
            .collection(COLLECTION_GROUPS)
            .document(groupId)
            .collection(COLLECTION_QUOTES)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(ResultDataModel.error(error))
                    return@addSnapshotListener
                }
                value?.let { snapShot ->
                    val groups = mutableListOf<QuoteDataModel>()
                    for (doc in snapShot) {
                        groups.add(doc.toObject())
                    }
                    trySend(ResultDataModel.success(groups))
                }
            }
        awaitClose {
            subscription.remove()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribeFrequencies() {
        launch {
            var subscription: ListenerRegistration? = null
            _frequenciesFlow.subscriptionCount.collect { subscribers ->
                Timber.tag(TAG).i("subscribeFrequencies: subscribers: $subscribers")
                if (subscribers > 0) {
                    subscription = dbInstance.collection(COLLECTION_FREQUENCY)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                _frequenciesFlow.tryEmit(ResultDataModel.error(error))
                                return@addSnapshotListener
                            }
                            value?.let { snapShot ->
                                val setting = mutableListOf<FrequencyDataModel>()
                                for (doc in snapShot) {
                                    setting.add(doc.toObject())
                                }
                                _frequenciesFlow.tryEmit(ResultDataModel.success(setting))
                            }
                        }
                } else {
                    subscription?.let {
                        it.remove()
                        _frequenciesFlow.resetReplayCache()
                        cancel()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribeUserFrequency() {
        launch {
            var subscription: ListenerRegistration? = null
            _userFrequencyFlow.subscriptionCount.collect { subscribers ->
                Timber.tag(TAG).i("subscribeUserFrequency: subscribers: $subscribers")
                if (subscribers > 0) {
                    subscription = dbInstance.collection(COLLECTION_PERSONAL)
                        .document(localDataSource.token)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                _userFrequencyFlow.tryEmit(ResultDataModel.error(error))
                                return@addSnapshotListener
                            }
                            val frequency = value?.data?.get(FREQUENCY_FIELD) as? Long
                            localDataSource.setFrequency(frequency ?: DEFAULT_FREQUENCY_VALUE)
                            _userFrequencyFlow.tryEmit(ResultDataModel.success(frequency))
                        }
                } else {
                    subscription?.let {
                        it.remove()
                        _userFrequencyFlow.resetReplayCache()
                        cancel()
                    }
                }
            }
        }
    }


    private fun selectedQuotes(groupId: String?, token: String?): Flow<ResultDataModel<List<SelectedQuoteDataModel>>> = callbackFlow {
        if (groupId == null) {
            trySend(ResultDataModel.error(Exception("Group id is null")))
            return@callbackFlow
        }
        if (token == null) {
            trySend(ResultDataModel.error(Exception("Token is null")))
            return@callbackFlow
        }

        val subscription =
            dbInstance.collection(COLLECTION_PERSONAL)
                .document(token)
                .collection(COLLECTION_SELECTION)
                .document(groupId)
                .collection(COLLECTION_SELECTED_QUOTES)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(ResultDataModel.error(error))
                        return@addSnapshotListener
                    }
                    value?.let {
                        val selections = mutableListOf<SelectedQuoteDataModel>()
                        for (doc in value) {
                            selections.add(doc.toObject())
                        }
                        trySend(
                            ResultDataModel.success(selections)
                        )
                    }
                }
        awaitClose {
            subscription.remove()
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
        private const val QUOTES_COUNT_FIELD = "quotesCount"
        private const val GROUP_NAME_FIELD = "name"
        private const val GROUP_DESCRIPTION_FIELD = "description"
        private const val QUOTE_QUOTE_FIELD = "quote"
        private const val QUOTE_AUTHOR_FIELD = "author"
    }
}
