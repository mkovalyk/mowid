package com.kovcom.mowid.model

import android.os.Parcelable
import com.kovcom.domain.model.Group
import kotlinx.parcelize.Parcelize

@Parcelize
data class UiGroup(
    val id: String,
    val name: String,
    val description: String,
    val count: Int,
    val selectedCount: Int,
    val canBeDeleted: Boolean,
) : Parcelable

fun UiGroup.toDomainModel() = Group(
    id = id,
    name = name,
    description = description,
    count = count,
    selectedCount = selectedCount,
    canBeDeleted = canBeDeleted
)

fun Group.toUIModel() = UiGroup(
    id = id,
    name = name,
    description = description,
    count = count,
    selectedCount = selectedCount,
    canBeDeleted = canBeDeleted
)

fun List<UiGroup>.toDomainModel() = map { it.toDomainModel() }

fun List<Group>.toUIModel() = map { it.toUIModel() }
