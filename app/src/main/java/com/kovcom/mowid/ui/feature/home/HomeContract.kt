package com.kovcom.mowid.ui.feature.home

import com.kovcom.domain.model.Group
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.model.UiGroup

data class HomeState(
    val isLoading: Boolean,
    val groupPhraseList: List<UiGroup>,
    val isLoggedIn: Boolean,
) : State

sealed interface HomeUserIntent : UserIntent {
    object SubscribeToList : HomeUserIntent

    //    object SubscribeToUser: HomeUserIntent
    object AddClicked : HomeUserIntent
    data class AddGroupClicked(val name: String, val description: String) : HomeUserIntent
    data class GroupItemClicked(val groupPhrase: UiGroup) : HomeUserIntent
    object HideGroupModal : HomeUserIntent
    data class OnEditClicked(
        val id: String,
        val editedName: String,
        val editedDescription: String,
    ) : HomeUserIntent

    data class ShowGroupModal(val id: String, val name: String, val description: String) :
        HomeUserIntent

    data class OnItemDeleted(val id: String, val name: String) : HomeUserIntent
    data class RemoveGroupConfirmed(val id:String, val name: String) : HomeUserIntent
}

sealed interface HomeEvent : Event {
    data class OnItemDeleted(val name: String) : HomeEvent
    data class ShowGroupModal(val id: String, val name: String, val description: String) :
        HomeEvent
    object ShowAddGroupModal: HomeEvent

    object HideGroupModal : HomeEvent
    object ShowLoginScreen : HomeEvent
    data class ShowError(val message: String) : HomeEvent
    data class ShowSnackbar(val message: String) : HomeEvent
    data class ItemClicked(val groupPhrase: UiGroup) : HomeEvent
    data class ShowRemoveConfirmationDialog(val id: String, val name: String) : HomeEvent
}

sealed interface HomeEffect : Effect {
    data class Loading(val isLoading: Boolean) : HomeEffect
    data class Loaded(val information: List<Group>) : HomeEffect
    data class ShowError(val message: String) : HomeEffect
    data class UserLoaded(val isLoggedIn: Boolean) : HomeEffect
    data class ShowEditGroupModal(val id: String, val name: String, val description: String) :
        HomeEffect

    data class OpenDetails(val groupPhrase: UiGroup) : HomeEffect
    data class RemoveGroup(val id: String, val name: String) : HomeEffect
    data class RemoveGroupConfirmed(val name: String) : HomeEffect
    object ShowAddGroupModel : HomeEffect
    object ShowLoginScreen : HomeEffect
    object ShowGroupCreatedMessage : HomeEffect
    object HideGroupModal : HomeEffect
}
