package com.kovcom.data.model

import com.kovcom.domain.model.GroupType

data class GroupModel(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val quotesCount: Int = 0,
    val groupType: GroupType = GroupType.Common,
)
