package com.kovcom.data.model

data class SelectedGroupModel(
    val groupId: String? = null,
    val quotesIds: List<String> = emptyList(),
) {

    val selectedQuotesCount
        get() = quotesIds.size
}

