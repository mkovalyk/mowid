package com.kovcom.mowid.ui.feature.home

import com.kovcom.domain.model.Group
import com.kovcom.domain.model.GroupType
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.model.UiGroup

data class HomeState(
    val isLoading: Boolean,
    val groupList: List<UiGroup>,
    val isLoggedIn: Boolean,
    val dialogType: DialogType = DialogType.None,
) : IState {

    sealed class DialogType {
        data class RemoveGroupConfirmation(
            val id: String,
            val groupType: GroupType,
            val name: String,
        ) : DialogType()

        data object LoginToProceed : DialogType()
        data object None : DialogType()
    }

    companion object {

        val EMPTY = HomeState(
            isLoading = true,
            groupList = emptyList(),
            isLoggedIn = false,
        )
    }
}

sealed interface HomeUserIntent : UserIntent {
    data object AddClicked : HomeUserIntent
    data class AddGroupClicked(val name: String, val description: String) : HomeUserIntent
    data class GroupItemClicked(val groupPhrase: UiGroup) : HomeUserIntent
    data object HideGroupModal : HomeUserIntent
    data class OnEditClicked(
        val id: String,
        val editedName: String,
        val editedDescription: String,
    ) : HomeUserIntent

    data class ShowGroupModal(val id: String, val name: String, val description: String) :
        HomeUserIntent

    data class OnItemDelete(val id: String, val groupType: GroupType, val name: String) :
        HomeUserIntent

    data class RemoveGroupConfirmed(val id: String, val groupType: GroupType, val name: String) :
        HomeUserIntent

    data object HideGroupConfirmationDialog : HomeUserIntent
    data class OnGroupSelectionChanged(val id: String, val isSelected: Boolean, val groupType: GroupType) : HomeUserIntent
}

sealed interface HomeEvent : IEvent {
    data class OnItemDeleted(val name: String) : HomeEvent
    data class ShowGroupModal(val id: String, val name: String, val description: String) :
        HomeEvent

    data object ShowAddGroupModal : HomeEvent

    data object HideGroupModal : HomeEvent
    data object ShowLoginScreen : HomeEvent
    data class ShowError(val message: String) : HomeEvent
    data class ShowSnackbar(val message: String) : HomeEvent
    data class ItemClicked(val groupPhrase: UiGroup) : HomeEvent
    data class ShowGroupSelectionChangedMessage(val selected: Boolean) : HomeEvent
}

sealed interface HomeEffect : IEffect {
    data class Loading(val isLoading: Boolean) : HomeEffect
    data class Loaded(val information: List<Group>) : HomeEffect
    data class ShowError(val message: String) : HomeEffect
    data class UserLoaded(val isLoggedIn: Boolean) : HomeEffect
    data class ShowEditGroupModal(val id: String, val name: String, val description: String) :
        HomeEffect

    data class OpenDetails(val groupPhrase: UiGroup) : HomeEffect
    data class RemoveGroup(val id: String, val groupType: GroupType, val name: String) : HomeEffect
    data class RemoveGroupConfirmed(val name: String) : HomeEffect
    data object DismissRemoveGroupConfirmation : HomeEffect
    data object ShowAddGroupModel : HomeEffect
    data object ShowLoginScreen : HomeEffect
    data object ShowGroupCreatedMessage : HomeEffect
    data class ShowGroupSelectionMessage(val selected: Boolean) : HomeEffect
    data object HideGroupModal : HomeEffect
}
