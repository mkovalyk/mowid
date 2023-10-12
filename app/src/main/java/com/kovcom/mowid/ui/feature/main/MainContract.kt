package com.kovcom.mowid.ui.feature.main

import androidx.annotation.StringRes
import com.kovcom.mowid.base.ui.Effect
import com.kovcom.mowid.base.ui.Event
import com.kovcom.mowid.base.ui.State

sealed class MainState : State {
    data class Loading(val state: Boolean) : MainState()
}

sealed class MainEvent : Event {
    object SignIn : MainEvent()
    object SignOut : MainEvent()
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainEvent()
}

sealed class MainEffect : Effect {
    data class ShowToast(@StringRes val messageId: Int) : MainEffect()
}