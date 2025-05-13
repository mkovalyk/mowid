package com.kovcom.data.model

data class QuoteModel(
    val id: String = "",
    val quote: String = "",
    val author: String = "",
    val created: String? = null,
    val canBeDeleted: Boolean = false,
)
