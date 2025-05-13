package com.kovcom.data.firebase.source

import com.kovcom.data.model.*
import kotlinx.coroutines.flow.Flow


interface CommonGroupsDataSource {

    val groupsFlow: Flow<Result<List<GroupModel>>>

    val quotesFlow: Flow<Result<List<QuoteModel>>>

    val selectedLocaleFlow: Flow<Result<String>>

    val localesFlow: Flow<Result<List<LocaleModel>>>

    suspend fun selectLocale(locale: LocaleModel): Result<String>

    suspend fun selectGroup(groupId: String)

    suspend fun getQuoteById(groupId: String, quoteId: String): Result<QuoteModel>

    suspend fun getQuotesForGroup(groupId: String): Result<List<QuoteModel>>
}
