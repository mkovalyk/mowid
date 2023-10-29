package com.kovcom.data.model

data class GroupModel(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val quotesCount: Int = 0,
    val canBeDeleted: Boolean = false,
    val groupType: GroupType = GroupType.Common,
)
