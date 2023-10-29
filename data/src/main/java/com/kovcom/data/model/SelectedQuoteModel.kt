package com.kovcom.data.model

data class SelectedQuoteModel(
    val id: String = "",
    val groupId: String = "",
    val quote: String? = null,
    val author: String? = null,
    val shownAt: Long? = null,
    val selectedBy: String? = null,
)
