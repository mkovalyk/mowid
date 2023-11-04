package com.kovcom.data.mapper

import com.kovcom.data.model.GroupModel
import com.kovcom.data.model.SelectedGroupModel
import com.kovcom.domain.model.Group
import com.kovcom.domain.model.GroupType

fun GroupModel.mapToDomain(selectedGroups: List<SelectedGroupModel>) = Group(
    id = id.orEmpty(),
    name = name,
    description = description,
    count = quotesCount ?: 0,
    selectedCount = calculateSelectedCount(this, selectedGroups),
    canBeDeleted = groupType == GroupType.Common,
)

fun calculateSelectedCount(
    groupModel: GroupModel,
    selectedGroups: List<SelectedGroupModel>,
): Int {
    selectedGroups.firstOrNull { groupModel.id == it.groupId }?.let { group ->
        return group.selectedQuotesCount ?: 0
    }
    return 0
}
