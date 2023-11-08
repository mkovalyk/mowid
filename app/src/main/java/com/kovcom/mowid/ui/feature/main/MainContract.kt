package com.kovcom.mowid.ui.feature.main

import androidx.annotation.StringRes
import com.kovcom.mowid.base.ui.*

sealed class MainState : State {
    data class Loading(val state: Boolean) : MainState()
}

sealed interface MainUserIntent : UserIntent {
    object SignIn : MainUserIntent
    object SignOut : MainUserIntent
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainUserIntent
}

sealed class MainEvent : Event {
    data class ShowToast(@StringRes val messageId: Int) : MainEffect()
}

sealed class MainEffect : Effect {
    data class ShowToast(@StringRes val messageId: Int) : MainEffect()
}