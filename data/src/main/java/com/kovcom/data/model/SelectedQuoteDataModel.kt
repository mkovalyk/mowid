package com.kovcom.data.model

data class SelectedQuoteDataModel(
    val id: String = "",
    val groupId: String = "",
    val shownAt: Long? = null,
    val selectedByToken: String? = null,
)
