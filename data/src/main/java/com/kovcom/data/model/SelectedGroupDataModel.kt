package com.kovcom.data.model

data class SelectedGroupDataModel(
    val groupId: String? = null,
    val quoteIds: List<String> = emptyList(),
) {

    val selectedQuotesCount = quoteIds.size
}

