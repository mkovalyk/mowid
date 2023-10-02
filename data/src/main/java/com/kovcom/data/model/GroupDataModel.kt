package com.kovcom.data.model

data class GroupDataModel(
    var id: String? = null,
    val name: String? = null,
    val description: String? = null,
    var quotesCount: Int? = null,
    val canBeDeleted: Boolean? = null
)
