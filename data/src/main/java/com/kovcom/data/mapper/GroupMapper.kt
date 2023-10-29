package com.kovcom.data.mapper

import com.kovcom.data.model.GroupDataModel
import com.kovcom.data.model.SelectedGroupDataModel
import com.kovcom.domain.model.Group

fun GroupDataModel.mapToDomain(selectedGroups: List<SelectedGroupDataModel>) = Group(
    id = id.orEmpty(),
    name = name.orEmpty(),
    description = description.orEmpty(),
    count = quotesCount ?: 0,
    selectedCount = calculateSelectedCount(this, selectedGroups),
    canBeDeleted = canBeDeleted ?: false
)

fun calculateSelectedCount(
    groupDataModel: GroupDataModel,
    selectedGroups: List<SelectedGroupDataModel>
): Int {
    selectedGroups.firstOrNull { groupDataModel.id == it.groupId }?.let { group ->
        return group.selectedQuotesCount ?: 0
    }
    return 0
}
