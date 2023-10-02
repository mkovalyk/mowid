package com.kovcom.mowid.model

import com.kovcom.domain.model.GroupPhraseModel

data class GroupPhraseUIModel(
    val id: String,
    val name: String,
    val description: String,
    val count: Int,
    val selectedCount: Int,
    val canBeDeleted: Boolean
)

fun GroupPhraseUIModel.toDomainModel() = GroupPhraseModel(
    id = id,
    name = name,
    description = description,
    count = count,
    selectedCount = selectedCount,
    canBeDeleted = canBeDeleted
)

fun GroupPhraseModel.toUIModel() = GroupPhraseUIModel(
    id = id,
    name = name,
    description = description,
    count = count,
    selectedCount = selectedCount,
    canBeDeleted = canBeDeleted
)

fun List<GroupPhraseUIModel>.toDomainModel() = map { it.toDomainModel() }

fun List<GroupPhraseModel>.toUIModel() = map { it.toUIModel() }
