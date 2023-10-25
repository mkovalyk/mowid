package com.kovcom.domain.repository

import com.kovcom.domain.model.FrequenciesModel
import com.kovcom.domain.model.GroupPhraseModel
import com.kovcom.domain.model.QuoteModel
import kotlinx.coroutines.flow.Flow

interface MotivationPhraseRepository {

    fun getGroupsFlow(): Flow<List<GroupPhraseModel>>

    fun getQuotes(groupId: String): Flow<List<QuoteModel>>

    fun getFrequencySettingsFlow(): Flow<FrequenciesModel>

    suspend fun addGroup(name: String, description: String)

    suspend fun addQuote(groupId: String, quote: String, author: String, quoteId: String)

    suspend fun updateUserFrequency(id: Long)

    suspend fun deleteQuote(groupId: String, quoteId: String, isSelected: Boolean)

    suspend fun deleteGroup(id: String)

    suspend fun selectGroup(groupId: String)

    suspend fun saveSelection(
        groupId: String,
        quoteId: String,
        isSelected: Boolean,
    )

    suspend fun editQuote(
        groupId: String,
        quoteId: String,
        editedQuote: String,
        editedAuthor: String,
    )

    suspend fun editGroup(
        groupId: String,
        editedName: String,
        editedDescription: String,
    )
}
