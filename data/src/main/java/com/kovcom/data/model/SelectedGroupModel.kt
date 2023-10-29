package com.kovcom.data.model

data class SelectedGroupModel(
    val groupId: String? = null,
    val quoteIds: List<String> = emptyList(),
) {

    val selectedQuotesCount = quoteIds.size
}

