package com.kovcom.mowid.ui.feature.home

import com.kovcom.mowid.base.ui.UiEffect
import com.kovcom.mowid.base.ui.UiEvent
import com.kovcom.mowid.base.ui.UiState
import com.kovcom.mowid.model.GroupPhraseUIModel

data class HomeState(
    val isLoading: Boolean,
    val groupPhraseList: List<GroupPhraseUIModel>,
    val isLoggedIn: Boolean,
) : UiState

sealed class HomeEvent : UiEvent {
    data class GroupItemClicked(val groupPhrase: GroupPhraseUIModel) : HomeEvent()
    data class OnItemDeleted(val id: String) : HomeEvent()
    object AddClicked : HomeEvent()
    object ShowGroupModal : HomeEvent()
    object HideGroupModal : HomeEvent()
    data class AddGroupClicked(val name: String, val description: String) : HomeEvent()
    data class OnEditClicked(
        val id: String,
        val editedName: String,
        val editedDescription: String,
    ) : HomeEvent()
    object ShowLoginScreen : HomeEvent()
}

sealed class HomeEffect : UiEffect {
    data class ShowError(val message: String) : HomeEffect()
    data class ShowEditGroupModal(val groupPhrase: GroupPhraseUIModel) : HomeEffect()
    object ShowAddGroupModel : HomeEffect()
    object ShowLoginScreen : HomeEffect()
}