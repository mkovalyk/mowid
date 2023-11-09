package com.kovcom.data.repository

import com.kovcom.data.firebase.source.CommonGroupsDataSource
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl
import com.kovcom.data.mapper.mapToDomain
import com.kovcom.data.mapper.toDomain
import com.kovcom.data.model.GroupModel
import com.kovcom.data.model.QuoteModel
import com.kovcom.data.model.Result
import com.kovcom.data.model.SelectedQuoteModel
import com.kovcom.data.model.merge
import com.kovcom.domain.model.Frequencies
import com.kovcom.domain.model.Group
import com.kovcom.domain.model.GroupType
import com.kovcom.domain.model.Quote
import com.kovcom.domain.repository.QuotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuotesRepositoryImpl(
    private val firebaseDataSource: FirebaseDataSource,
    private val commonGroupsDataSource: CommonGroupsDataSource,
) : QuotesRepository {

    override fun getGroupsFlow(): Flow<List<Group>> = combine(
        commonGroupsDataSource.groupsFlow,
        firebaseDataSource.userGroupsFlow,
        firebaseDataSource.selectedGroupsFlow
    ) { groups, userGroups, selectedGroups ->

        Timber.tag(TAG).i(
            "getGroupsFlow. Groups: ${groups.data?.size}. userGroups: ${userGroups.data?.size} " +
                    "selectedGroups: ${selectedGroups.data?.size}"
        )
        val allGroups = groups.merge(userGroups)
        if (selectedGroups is Result.Error) {
            throw selectedGroups.error ?: Exception("Unknown exception")
        }
        when (allGroups) {
            is Result.Success -> allGroups.data?.distinctBy { it.id }
                ?.map { model -> model.mapToDomain(selectedGroups.data.orEmpty()) }
                ?: emptyList()

            is Result.Error -> throw allGroups.error ?: Exception("Unknown exception")
        }
    }

    override fun getQuotes(groupId: String): Flow<List<Quote>> {

        firebaseDataSource.subscribeAllGroupsQuotes(groupId)
        return combine(
            commonGroupsDataSource.quotesFlow,
            firebaseDataSource.userQuotesFlow,
            firebaseDataSource.selectedGroupsFlow
        ) { quotes, userQuotes, selectedQuotes ->
            Timber.tag(TAG).i(
                "getQuotesFlow. Quotes: ${quotes.data?.size}. userQuotes: ${userQuotes.data?.size} " +
                        " selectedQuotes: ${selectedQuotes.data?.size}"
            )
            val allQuotes = quotes.merge(userQuotes)
            if (selectedQuotes is Result.Error) {
                throw selectedQuotes.error ?: Exception("Unknown exception")
            }
            when (allQuotes) {
                is Result.Success -> {
                    val selectedIds = selectedQuotes.data.orEmpty()
                        .flatMap { it.quotesIds.map { quote -> quote.quoteId }.toList() }.toSet()
                    allQuotes.data.orEmpty().map { model -> model.mapToDomain(selectedIds) }
                }

                is Result.Error -> throw allQuotes.error ?: Exception("Unknown exception")
            }
        }
    }

    override fun getFrequencySettingsFlow(): Flow<Frequencies> {
        return combine(
            firebaseDataSource.frequenciesFlow,
            firebaseDataSource.userFrequencyFlow
        ) { settings, userSettings ->
            if (settings is Result.Error) {
                throw settings.error ?: Exception("Unknown exception")
            }
            if (userSettings is Result.Error) {
                throw userSettings.error ?: Exception("Unknown exception")
            }
            settings.data?.toDomain(
                userSettings.data ?: FirebaseDataSourceImpl.DEFAULT_FREQUENCY_VALUE
            ) ?: throw Exception("Unknown exception")
        }
    }

    override suspend fun addGroup(name: String, description: String) {
        firebaseDataSource.saveNewGroup(
            GroupModel(
                name = name,
                description = description,
                groupType = GroupType.Personal,
            )
        )
    }

    override suspend fun addQuote(groupId: String, quote: String, author: String, quoteId: String) {
        Timber.tag(TAG).i("addQuote: $quoteId -> $groupId -> $quote -> $author")
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        
        firebaseDataSource.saveNewQuote(
            groupId,
            QuoteModel(
                id = quoteId,
                author = author,
                quote = quote,
                created = format.format(Date()),
                canBeDeleted = true
            )
        )
        firebaseDataSource.saveSelection(
            quote = SelectedQuoteModel(
                id = quoteId,
                groupId = groupId,
            ),
            isSelected = true,
            groupType = GroupType.Personal
        )
    }

    override suspend fun updateUserFrequency(id: Long) {
        firebaseDataSource.updateUserFrequency(id)
    }

    override suspend fun deleteQuote(groupId: String, quoteId: String, isSelected: Boolean) {
        firebaseDataSource.deleteQuote(groupId, quoteId, isSelected)
    }

    override suspend fun deleteGroup(id: String, groupType: GroupType) {
        when(groupType) {
            GroupType.Personal -> firebaseDataSource.deleteGroup(id)
            GroupType.Common -> Unit // TODO might
        }
    }

    override suspend fun selectGroup(groupId: String) {
        commonGroupsDataSource.selectGroup(groupId)
        firebaseDataSource.selectGroup(groupId)
    }

    override suspend fun saveSelection(
        groupId: String,
        quoteId: String,
        groupType: GroupType,
        isSelected: Boolean,
    ) {
        firebaseDataSource.saveSelection(
            quote = SelectedQuoteModel(
                id = quoteId,
                groupId = groupId,
            ),
            isSelected = isSelected,
            groupType = groupType,
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
