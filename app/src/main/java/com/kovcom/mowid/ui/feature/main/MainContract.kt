package com.kovcom.mowid.ui.feature.main

import androidx.annotation.StringRes
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.mowid.base.ui.Effect
import com.kovcom.mowid.base.ui.Event
import com.kovcom.mowid.base.ui.State
import com.kovcom.mowid.base.ui.UserIntent

sealed class MainState : State {
    object Loading : MainState()
}

sealed interface MainUserIntent : UserIntent {
    object SignIn : MainUserIntent
    object SignOut : MainUserIntent
    data class SignInSuccess(val firebaseAuthResult: FirebaseAuthUIAuthenticationResult) :
        MainUserIntent

    object SignOutSuccess : MainUserIntent
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainUserIntent
}

sealed interface MainEvent : Event {
    data class ShowToast(@StringRes val messageId: Int) : MainEvent
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainEvent
    object SignIn : MainEvent
    object SignOut : MainEvent
}

sealed interface MainEffect : Effect {
    data class ShowToast(@StringRes val messageId: Int) : MainEffect
    object SignInSuccess : MainEffect
    object SignInError : MainEffect
    object SignIn : MainEffect
    object SignOut : MainEffect
    object SignOutSuccess : MainEffect
}
