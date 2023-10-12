package com.kovcom.mowid.ui.feature.home

import com.kovcom.domain.model.GroupPhraseModel
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.model.GroupPhraseUIModel

data class HomeState(
    val isLoading: Boolean,
    val groupPhraseList: List<GroupPhraseUIModel>,
    val isLoggedIn: Boolean,
) : State

sealed interface HomeUserIntent : UserIntent {
    object Load : HomeUserIntent
    object AddClicked : HomeUserIntent
    data class AddGroupClicked(val name: String, val description: String) : HomeUserIntent
    data class GroupItemClicked(val groupPhrase: GroupPhraseUIModel) : HomeUserIntent
    object HideGroupModal : HomeUserIntent
    data class OnEditClicked(
        val id: String,
        val editedName: String,
        val editedDescription: String,
    ) : HomeUserIntent
    object ShowGroupModal : HomeUserIntent
    data class OnItemDeleted(val id: String) : HomeUserIntent
}

sealed interface HomeEvent : Event {
    data class OnItemDeleted(val id: String) : HomeEvent
    object ShowGroupModal : HomeEvent
    object HideGroupModal : HomeEvent
    object ShowLoginScreen : HomeEvent
    data class ShowError(val message: String) : HomeEvent
    data class ShowSnackbar(val message: String) : HomeEvent
    data class ItemClicked(val groupPhrase: GroupPhraseUIModel) : HomeEvent
}

sealed interface HomeEffect : Effect {
    data class Loading(val isLoading: Boolean) : HomeEffect
    data class Loaded(val information: List<GroupPhraseModel>) : HomeEffect
    data class ShowError(val message: String) : HomeEffect
    data class ShowEditGroupModal(val groupPhrase: GroupPhraseUIModel) : HomeEffect
    object ShowAddGroupModel : HomeEffect
    object ShowLoginScreen : HomeEffect
    object ShowGroupCreatedMessage : HomeEffect
    object HideGroupModal : HomeEffect
}