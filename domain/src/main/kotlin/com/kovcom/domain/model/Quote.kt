package com.kovcom.domain.model

data class Quote(
    val id: String,
    val author: String,
    val created: String,
    val quote: String,
    val canBeDeleted: Boolean,
    val isSelected: Boolean,
)
