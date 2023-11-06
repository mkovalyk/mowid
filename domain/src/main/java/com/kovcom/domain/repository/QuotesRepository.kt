package com.kovcom.domain.repository

import com.kovcom.domain.model.*
import kotlinx.coroutines.flow.Flow

interface QuotesRepository {

    fun getGroupsFlow(): Flow<List<Group>>

    fun getQuotes(groupId: String): Flow<List<Quote>>

    fun getFrequencySettingsFlow(): Flow<Frequencies>

    suspend fun addGroup(name: String, description: String)

    suspend fun addQuote(groupId: String, quote: String, author: String, quoteId: String)

    suspend fun updateUserFrequency(id: Long)

    suspend fun deleteQuote(groupId: String, quoteId: String, isSelected: Boolean)

    suspend fun deleteGroup(id: String, groupType: GroupType)

    suspend fun selectGroup(groupId: String)

    suspend fun saveSelection(
        groupId: String,
        quoteId: String,
        groupType: GroupType,
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
