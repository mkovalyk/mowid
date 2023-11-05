package com.kovcom.data.firebase.source

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.kovcom.data.model.*
import com.kovcom.data.preferences.LocalDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CommonGroupsDataSourceImpl  constructor(
    private val dbInstance: FirebaseFirestore,
    private val localDataSource: LocalDataSource,
) : CommonGroupsDataSource, CoroutineScope {

    private val currentGroupFlow = MutableStateFlow<String?>(null)
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    override val selectedLocaleFlow: Flow<Result<String>> =
        localDataSource.selectedLocale.map {
            Result.success(it)
        }

    override val groupsFlow = selectedLocaleFlow.flatMapLatest { result -> 
        result.data?.let{
            groupsFlow(it)
        }?: flowOf(Result.error(result.error ?: Exception("Locale id is null")))

    }

    override val quotesFlow: Flow<Result<List<QuoteModel>>> =
        selectedLocaleFlow.combine(currentGroupFlow) { locale, groupId ->
            if (groupId == null) {
                flowOf(Result.error(Exception("Group id is null")))
            } else {
                quotesFlow(groupId, locale.data ?: "")
            }
        }.flattenConcat()

    override val localesFlow: Flow<Result<List<LocaleModel>>> = callbackFlow {
        val subscription = dbInstance.collection(COLLECTION_LOCALE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(Result.error(error))
                    return@addSnapshotListener
                }
                value?.let { snapShot ->
                    val locales = mutableListOf<LocaleModel>()
                    for (doc in snapShot) {
                        locales.add(doc.toObject())
                    }
                    trySend(Result.success(locales))
                }
            }
        awaitClose {
            subscription.remove()
            channel.close()
        }
    }

    override suspend fun selectLocale(locale: LocaleModel): Result<String> {
        localDataSource.setSelectedLocale(locale.id)
        return Result.success(locale.id)
    }

    override suspend fun selectGroup(groupId: String) {
        currentGroupFlow.value = groupId
    }

    private fun groupsFlow(selectedLocaleId: String): Flow<Result<List<GroupModel>>> =
        callbackFlow {
            val subscription =
                dbInstance.collection(COLLECTION_LOCALE)
                    .document(selectedLocaleId)
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
                            trySend(Result.success(groups))
                        }
                    }
            awaitClose {
                subscription.remove()
                channel.close()
            }
        }

    private fun quotesFlow(
        groupId: String,
        selectedLocaleId: String,
    ): Flow<Result<List<QuoteModel>>> =
        callbackFlow {
            val subscription = dbInstance
                .collection(COLLECTION_LOCALE)
                .document(selectedLocaleId)
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

    companion object {

        private const val COLLECTION_GROUPS = "groups"
        private const val COLLECTION_QUOTES = "quotes"
        private const val COLLECTION_LOCALE = "locale"
    }

}
