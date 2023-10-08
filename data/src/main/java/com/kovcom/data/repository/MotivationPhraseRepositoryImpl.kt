package com.kovcom.data.repository

import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.firebase.source.impl.FirebaseDataSourceImpl
import com.kovcom.data.mapper.mapToDomain
import com.kovcom.data.mapper.toDomain
import com.kovcom.data.model.*
import com.kovcom.domain.model.FrequenciesModel
import com.kovcom.domain.model.GroupPhraseModel
import com.kovcom.domain.model.QuoteModel
import com.kovcom.domain.repository.MotivationPhraseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MotivationPhraseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
) : MotivationPhraseRepository {

    override fun getGroupsFlow(): Flow<List<GroupPhraseModel>> = combine(
        firebaseDataSource.groupsFlow,
        firebaseDataSource.userGroupsFlow,
        firebaseDataSource.selectedGroupsFlow
    ) { groups, userGroups, selectedGroups ->

        Timber.tag(TAG).i(
            "getGroupsFlow. Groups: ${groups.data?.size}. userGroups: ${userGroups.data?.size} " +
                "selectedGroups: ${selectedGroups.data?.size}"
        )
        val allGroups = groups.merge(userGroups)
        if (selectedGroups.status == Status.ERROR) {
            throw selectedGroups.error ?: Exception("Unknown exception")
        }
        when (allGroups.status) {
            Status.SUCCESS -> allGroups.data?.distinctBy { it.id }
                ?.map { model -> model.mapToDomain(selectedGroups.data.orEmpty()) }
                ?: emptyList()

            Status.ERROR -> throw allGroups.error ?: Exception("Unknown exception")
        }
    }

    override fun getQuotes(groupId: String): Flow<List<QuoteModel>> {

        firebaseDataSource.subscribeAllGroupsQuotes(groupId)
        return combine(
            firebaseDataSource.quotesFlow,
            firebaseDataSource.userQuotesFlow,
            firebaseDataSource.selectedQuotesFlow
        ) { quotes, userQuotes, selectedQuotes ->
            Timber.tag(TAG).i(
                "getQuotesFlow. Quotes: ${quotes.data?.size}. userQuotes: ${userQuotes.data?.size} " +
                    " selectedQuotes: ${selectedQuotes.data?.size}"
            )
            val allQuotes = quotes.merge(userQuotes)
            if (selectedQuotes.status == Status.ERROR) {
                throw selectedQuotes.error ?: Exception("Unknown exception")
            }
            when (allQuotes.status) {
                Status.SUCCESS -> {
                    val selectedIds = selectedQuotes.data.orEmpty().map { it.id }.toSet()
                    allQuotes.data.orEmpty().map { model -> model.mapToDomain(selectedIds) }
                }

                Status.ERROR -> throw allQuotes.error ?: Exception("Unknown exception")
            }
        }
    }

    override fun getFrequencySettingsFlow(): Flow<FrequenciesModel> {
        return combine(
            firebaseDataSource.frequenciesFlow,
            firebaseDataSource.userFrequencyFlow
        ) { settings, userSettings ->
            if (settings.status == Status.ERROR) {
                throw settings.error ?: Exception("Unknown exception")
            }
            if (userSettings.status == Status.ERROR) {
                throw userSettings.error ?: Exception("Unknown exception")
            }
            settings.data?.toDomain(
                userSettings.data ?: FirebaseDataSourceImpl.DEFAULT_FREQUENCY_VALUE
            )
                ?: throw Exception("Unknown exception")
        }
    }

    override suspend fun addGroup(name: String, description: String) {
        firebaseDataSource.saveNewGroup(
            GroupDataModel(
                name = name,
                description = description,
                canBeDeleted = true
            )
        )
    }

    override suspend fun addQuote(groupId: String, quote: String, author: String) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        firebaseDataSource.saveNewQuote(
            groupId,
            QuoteDataModel(
                author = author,
                quote = quote,
                created = format.format(Date()),
                canBeDeleted = true
            )
        )
    }

    override suspend fun updateUserFrequency(id: Long) {
        firebaseDataSource.updateUserFrequency(id)
    }

    override suspend fun deleteQuote(groupId: String, quoteId: String, isSelected: Boolean) {
        firebaseDataSource.deleteQuote(groupId, quoteId, isSelected)
    }

    override suspend fun deleteGroup(id: String) {
        firebaseDataSource.deleteGroup(id)
    }

    override suspend fun saveSelection(
        groupId: String,
        quoteId: String,
        isSelected: Boolean,
    ) {
        firebaseDataSource.saveSelection(
            quote = SelectedQuoteDataModel(
                id = quoteId,
                groupId = groupId,
            ),
            isSelected = isSelected
        )
    }

    override suspend fun editQuote(
        groupId: String,
        quoteId: String,
        editedQuote: String,
        editedAuthor: String,
    ) {
        firebaseDataSource.editQuote(
            groupId = groupId,
            quoteId = quoteId,
            editedQuote = editedQuote,
            editedAuthor = editedAuthor
        )
    }

    override suspend fun editGroup(
        groupId: String,
        editedName: String,
        editedDescription: String,
    ) {
        firebaseDataSource.editGroup(
            groupId = groupId,
            editedName = editedName,
            editedDescription = editedDescription
        )
    }

    companion object {

        const val TAG = "MotivationPhraseRepository"
    }
}
