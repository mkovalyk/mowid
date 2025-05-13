package com.kovcom.mowid.model

import android.os.Parcelable
import com.kovcom.domain.model.Group
import com.kovcom.domain.model.GroupType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.min

@Parcelize
data class UiGroup(
    val id: String,
    val name: String,
    val description: String,
    val count: Int,
    val selectedCount: Int,
    val groupType: GroupType,
    val isSwipeToDeleteOpened: Boolean = false,
) : Parcelable {

    @IgnoredOnParcel
    val canBeDeleted: Boolean = groupType == GroupType.Personal

    @IgnoredOnParcel
    val combinedValue = "${min(selectedCount, count)}/$count"

    @IgnoredOnParcel
    val isAllSelected = count != 0 && count == min(selectedCount, count)
}

fun UiGroup.toDomainModel() = Group(
    id = id,
    name = name,
    description = description,
    count = count,
    selectedCount = selectedCount,
    groupType = groupType,
)

fun Group.toUIModel() = UiGroup(
    id = id,
    name = name,
    description = description,
    count = count,
    selectedCount = selectedCount,
    groupType = groupType,
)

fun List<UiGroup>.toDomainModel() = map { it.toDomainModel() }

fun List<Group>.toUIModel() = map { it.toUIModel() }
