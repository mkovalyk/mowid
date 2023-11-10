package com.kovcom.mowid.ui.feature.main

import androidx.annotation.StringRes
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.mowid.base.ui.IEffect
import com.kovcom.mowid.base.ui.IEvent
import com.kovcom.mowid.base.ui.IState
import com.kovcom.mowid.base.ui.UserIntent

sealed class MainState : IState {
    data object Loading : MainState()
}

sealed interface MainUserIntent : UserIntent {
    data object SignIn : MainUserIntent
    data object SignOut : MainUserIntent
    data class SignInSuccess(val firebaseAuthResult: FirebaseAuthUIAuthenticationResult) :
        MainUserIntent

    data object SignOutSuccess : MainUserIntent
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainUserIntent
}

sealed interface MainEvent : IEvent {
    data class ShowToast(@StringRes val messageId: Int) : MainEvent
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainEvent
    data object SignIn : MainEvent
    data object SignOut : MainEvent
}

sealed interface MainEffect : IEffect {
    data class ShowToast(@StringRes val messageId: Int) : MainEffect
    data object SignInSuccess : MainEffect
    data object SignInError : MainEffect
    data object SignIn : MainEffect
    data object SignOut : MainEffect
    data object SignOutSuccess : MainEffect
}
