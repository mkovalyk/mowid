package com.kovcom.data.firebase.source

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.kovcom.data.model.GroupDataModel
import com.kovcom.data.model.LocaleDataModel
import com.kovcom.data.model.QuoteDataModel
import com.kovcom.data.model.ResultDataModel
import com.kovcom.data.preferences.LocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

class CommonGroupsDataSourceImpl  constructor(
    private val dbInstance: FirebaseFirestore,
    private val localDataSource: LocalDataSource,
) : CommonGroupsDataSource, CoroutineScope {

    private val currentGroupFlow = MutableStateFlow<String?>(null)
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    override val selectedLocaleFlow: Flow<ResultDataModel<String>> =
        localDataSource.selectedLocale.map {
            ResultDataModel.success(it)
        }

    override val groupsFlow = selectedLocaleFlow.flatMapLatest {
        if (it.data != null) {
            groupsFlow(it.data)
        } else {
            flowOf(ResultDataModel.error(it.error ?: Exception("Locale id is null")))
        }
    }

    override val quotesFlow: Flow<ResultDataModel<List<QuoteDataModel>>> =
        currentGroupFlow.flatMapConcat { groupId ->
            if (groupId == null) {
                flowOf(ResultDataModel.error(Exception("Group id is null")))
            } else {
                quotesFlow(groupId)
            }
        }

    override val localesFlow: Flow<ResultDataModel<List<LocaleDataModel>>> = callbackFlow {
        val subscription = dbInstance.collection(COLLECTION_LOCALE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(ResultDataModel.error(error))
                    return@addSnapshotListener
                }
                value?.let { snapShot ->
                    val locales = mutableListOf<LocaleDataModel>()
                    for (doc in snapShot) {
                        locales.add(doc.toObject())
                    }
                    trySend(ResultDataModel.success(locales))
                }
            }
        awaitClose {
            subscription.remove()
            channel.close()
        }
    }

    override suspend fun selectLocale(locale: LocaleDataModel): ResultDataModel<String> {
        localDataSource.setSelectedLocale(locale.id)
        return ResultDataModel.success(locale.id)
    }

    private fun groupsFlow(selectedLocaleId: String): Flow<ResultDataModel<List<GroupDataModel>>> =
        callbackFlow {
            val subscription =
                dbInstance.collection(COLLECTION_LOCALE)
                    .document(selectedLocaleId)
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
            awaitClose {
                subscription.remove()
                channel.close()
            }
        }

    private fun quotesFlow(groupId: String): Flow<ResultDataModel<List<QuoteDataModel>>> =
        callbackFlow {
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
                channel.close()
            }
        }

    companion object {

        private const val COLLECTION_GROUPS = "groups"
        private const val COLLECTION_QUOTES = "quotes"
        private const val COLLECTION_LOCALE = "locale"
    }

}
