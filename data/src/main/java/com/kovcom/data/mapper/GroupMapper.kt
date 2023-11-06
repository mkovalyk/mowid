package com.kovcom.data.mapper

import com.kovcom.data.model.GroupModel
import com.kovcom.data.model.SelectedGroupModel
import com.kovcom.domain.model.Group

fun GroupModel.mapToDomain(selectedGroups: List<SelectedGroupModel>) = Group(
    id = id.orEmpty(),
    name = name,
    description = description,
    count = quotesCount,
    selectedCount = calculateSelectedCount(this, selectedGroups),
    groupType = groupType,
)

fun calculateSelectedCount(
    groupModel: GroupModel,
    selectedGroups: List<SelectedGroupModel>,
): Int {
    selectedGroups.firstOrNull { groupModel.id == it.groupId }?.let { group ->
        return group.selectedQuotesCount
    }
    return 0
}
