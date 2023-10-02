package com.kovcom.data.preferences

import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

    val testValue: Flow<Boolean>
    fun setTestValue(value: Boolean)

    val quoteChangeOption: Flow<String?>
    fun setQuoteChangeOption(value: String?)

    val tokenFlow: Flow<String?>

    // It is a temporal solution to get token synchronously.
    @Deprecated("Use tokenFlow instead")
    val token: String
    fun setToken(value: String?)

    val frequency: Flow<Long>
    fun setFrequency(value: Long)
}
