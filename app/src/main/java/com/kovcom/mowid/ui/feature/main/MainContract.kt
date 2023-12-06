package com.kovcom.mowid.ui.feature.main

import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.mowid.base.ui.*

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
    data class ShowToast(val message: String) : MainEvent
    data class NavigateToQuote(val groupId: String, val quoteId: String) : MainEvent
    data object SignIn : MainEvent
    data object SignOut : MainEvent
}

sealed interface MainEffect : IEffect {
    data class ShowToast(val message: String) : MainEffect
    data object SignInSuccess : MainEffect
    data object SignInError : MainEffect
    data object SignIn : MainEffect
    data object SignOut : MainEffect
    data object SignOutSuccess : MainEffect
}
