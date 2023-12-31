package com.kovcom.data.firebase.source

import com.kovcom.data.model.*
import kotlinx.coroutines.flow.Flow


interface FirebaseDataSource {

    val groupsFlow: Flow<ResultDataModel<List<GroupDataModel>>>

    val userGroupsFlow: Flow<ResultDataModel<List<GroupDataModel>>>

    val selectedGroupsFlow: Flow<ResultDataModel<List<SelectedGroupDataModel>>>

    val selectedQuotesFlow: Flow<ResultDataModel<List<SelectedQuoteDataModel>>>

    val quotesFlow: Flow<ResultDataModel<List<QuoteDataModel>>>

    val userQuotesFlow: Flow<ResultDataModel<List<QuoteDataModel>>>

    val frequenciesFlow: Flow<ResultDataModel<List<FrequencyDataModel>>>

    val userFrequencyFlow: Flow<ResultDataModel<Long>>


    fun subscribeAllGroupsQuotes(groupId: String)

    fun subscribeFrequencySettings()

    suspend fun getSelectedQuotes(): ResultDataModel<List<SelectedQuoteDataModel>>

    suspend fun updateSelectedQuote(groupId: String, quoteId: String, shownTime: Long)

    suspend fun saveNewGroup(group: GroupDataModel): ResultDataModel<GroupDataModel>

    suspend fun updateUserFrequency(settingId: Long): ResultDataModel<Long>

    suspend fun saveNewQuote(
        groupId: String,
        quote: QuoteDataModel,
    ): ResultDataModel<QuoteDataModel>

    suspend fun deleteQuote(
        groupId: String,
        quoteId: String,
        isSelected: Boolean,
    ): ResultDataModel<String>

    suspend fun deleteGroup(groupId: String)

    suspend fun saveSelection(
        quote: SelectedQuoteDataModel,
        isSelected: Boolean,
    ): ResultDataModel<SelectedQuoteDataModel>

    suspend fun editQuote(
        groupId: String,
        quoteId: String,
        editedQuote: String,
        editedAuthor: String,
    ): ResultDataModel<String>

    suspend fun editGroup(
        groupId: String,
        editedName: String,
        editedDescription: String,
    ): ResultDataModel<String>
}
