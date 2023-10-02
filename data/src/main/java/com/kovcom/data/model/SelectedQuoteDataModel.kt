package com.kovcom.data.model

data class SelectedQuoteDataModel(
    val id: String = "",
    val groupId: String = "",
    val quote: String? = null,
    val author: String? = null,
    val shownAt: Long? = null,
    var selectedBy: String? = null,
)
