package com.kovcom.data.model

data class QuoteModel(
    var id: String? = null,
    val quote: String = "",
    val author: String = "",
    val created: String? = null,
    val canBeDeleted: Boolean = false,
)
