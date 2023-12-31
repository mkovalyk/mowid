package com.kovcom.mowid.ui.feature.main

import androidx.annotation.StringRes
import com.kovcom.mowid.base.ui.UiEffect
import com.kovcom.mowid.base.ui.UiEvent
import com.kovcom.mowid.base.ui.UiState

sealed class MainState : UiState {
    data class Loading(val state: Boolean) : MainState()
}

sealed class MainEvent : UiEvent {
    object SignIn : MainEvent()
    object SignOut : MainEvent()
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainEvent()
}

sealed class MainEffect : UiEffect {
    data class ShowToast(@StringRes val messageId: Int) : MainEffect()
}