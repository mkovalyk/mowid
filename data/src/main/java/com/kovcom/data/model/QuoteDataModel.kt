package com.kovcom.data.model

data class QuoteDataModel(
    var id: String? = null,
    val quote: String? = null,
    val author: String? = null,
    val created: String? = null,
    val canBeDeleted: Boolean = false,
)
