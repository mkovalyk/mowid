package com.kovcom.mowid.ui.feature.main

import androidx.annotation.StringRes
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.mowid.base.ui.IEffect
import com.kovcom.mowid.base.ui.IEvent
import com.kovcom.mowid.base.ui.IState
import com.kovcom.mowid.base.ui.UserIntent

sealed class MainState : IState {
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

sealed interface MainEvent : IEvent {
    data class ShowToast(@StringRes val messageId: Int) : MainEvent
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainEvent
    object SignIn : MainEvent
    object SignOut : MainEvent
}

sealed interface MainEffect : IEffect {
    data class ShowToast(@StringRes val messageId: Int) : MainEffect
    object SignInSuccess : MainEffect
    object SignInError : MainEffect
    object SignIn : MainEffect
    object SignOut : MainEffect
    object SignOutSuccess : MainEffect
}
