package com.kovcom.domain.model

data class GroupPhraseModel(
    val id: String,
    val name: String,
    val description: String,
    val count: Int,
    val selectedCount: Int,
    val canBeDeleted: Boolean
)
