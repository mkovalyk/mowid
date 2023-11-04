package com.kovcom.data.firebase.source

import com.kovcom.data.model.*
import com.kovcom.domain.model.GroupType
import kotlinx.coroutines.flow.Flow

interface CommonGroupsDataSource {

    val groupsFlow: Flow<Result<List<GroupModel>>>

    val quotesFlow: Flow<Result<List<QuoteModel>>>

    val selectedLocaleFlow: Flow<Result<String>>

    val localesFlow: Flow<Result<List<LocaleModel>>>

    suspend fun selectLocale(locale: LocaleModel): Result<String>

    suspend fun selectGroup(groupId: String)
}


interface FirebaseDataSource {

    val userGroupsFlow: Flow<Result<List<GroupModel>>>

    val selectedGroupsFlow: Flow<Result<List<SelectedGroupModel>>>

    val userQuotesFlow: Flow<Result<List<QuoteModel>>>

    val frequenciesFlow: Flow<Result<List<FrequencyModel>>>

    val userFrequencyFlow: Flow<Result<Long>>

    fun subscribeAllGroupsQuotes(groupId: String)

    suspend fun getSelectedQuotes(): Result<List<SelectedQuoteModel>>

    suspend fun getQuoteById(groupId: String, groupType: GroupType, quoteId: String): Result<QuoteModel>

    suspend fun updateSelectedQuote(groupId: String, quoteId: String, shownTime: Long)

    suspend fun saveNewGroup(group: GroupModel): Result<GroupModel>

    suspend fun updateUserFrequency(settingId: Long): Result<Long>

    suspend fun saveNewQuote(
        groupId: String,
        quote: QuoteModel,
    ): Result<QuoteModel>

    suspend fun deleteQuote(
        groupId: String,
        quoteId: String,
        isSelected: Boolean,
    ): Result<String>

    suspend fun deleteGroup(groupId: String)
    suspend fun selectGroup(groupId: String)

    suspend fun saveSelection(
        quote: SelectedQuoteModel,
        groupType: GroupType,
        isSelected: Boolean,
    ): Result<SelectedQuoteModel>

    suspend fun editQuote(
        groupId: String,
        quoteId: String,
        editedQuote: String,
        editedAuthor: String,
    ): Result<String>

    suspend fun editGroup(
        groupId: String,
        editedName: String,
        editedDescription: String,
    ): Result<String>
}
