package com.kovcom.data.model

import com.kovcom.domain.model.GroupType

data class SelectedQuoteModel(
    val id: String = "",
    val groupId: String = "",
    val quote: String? = null,
    val author: String? = null,
    val shownAt: Long? = null,
    val groupType: GroupType = GroupType.Common,
    val selectedBy: String? = null,
)
