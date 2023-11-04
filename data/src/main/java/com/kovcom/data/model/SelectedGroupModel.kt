package com.kovcom.data.model

import com.kovcom.domain.model.GroupType

data class SelectedGroupModel(
    val groupId: String = "",
    val quotesIds: List<SelectedQuoteModelV2> = emptyList(),
    val groupType: GroupType = GroupType.Common,
) {

    val selectedQuotesCount
        get() = quotesIds.size
}

data class SelectedQuoteModelV2(
    val quoteId: String = "",
    val shownAt: Long = 0L,
)

