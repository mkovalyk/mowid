package com.kovcom.domain.model

data class Group(
    val id: String,
    val name: String,
    val description: String,
    val count: Int,
    val selectedCount: Int,
    val canBeDeleted: Boolean,
)
