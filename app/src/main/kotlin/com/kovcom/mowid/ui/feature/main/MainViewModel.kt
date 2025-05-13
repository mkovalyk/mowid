package com.kovcom.mowid.ui.feature.main

import android.app.Activity
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.Label
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import kotlinx.coroutines.flow.*

class MainViewModel(
    intentProcessor: IntentProcessor<MainState, MainUserIntent, MainEffect>,
    reducer: Reducer<MainEffect, MainState>,
    publisher: Publisher<MainEffect, MainEvent, MainState>,
) : BaseViewModel<MainState, MainEvent, MainEffect, MainUserIntent>(
    intentProcessor,
    reducer,
    publisher,
    initialState = MainState.Loading,
) {

    override fun tag() = "MainViewModel"

    class MainIntentProcessor(
        private val userRepository: UserRepository,
        private val workerManager: QuotesWorkerManager,
    ) : IntentProcessor<MainState, MainUserIntent, MainEffect> {

        override suspend fun processIntent(
            intent: MainUserIntent,
            currentState: MainState,
        ): Flow<MainEffect> {
            return when (intent) {
                is MainUserIntent.SignIn -> flowOf(MainEffect.SignIn)
                is MainUserIntent.SignOut -> flowOf(MainEffect.SignOut)
                is MainUserIntent.NavigateToQuote -> emptyFlow()
                is MainUserIntent.SignInSuccess -> signInResult(intent.firebaseAuthResult)
                is MainUserIntent.SignOutSuccess -> {
                    userRepository.signOutSuccess()
                    flowOf(MainEffect.SignOutSuccess)
                }
            }
        }

        private fun signInResult(result: FirebaseAuthUIAuthenticationResult): Flow<MainEffect> {
            return flow {
                if (result.resultCode == Activity.RESULT_OK) {
                    userRepository.signInSuccess()
                    workerManager.execute(ExecutionOption.Regular)
                    flowOf(MainEffect.ShowToast(Label.Sign.In.Success.value))
                } else {
                    flowOf(MainEffect.ShowToast(Label.Sign.In.Error.value))
                }
            }
        }
    }

    class MainEventReducer : Reducer<MainEffect, MainState> {

        override fun reduce(effect: MainEffect, state: MainState): MainState {
            return when (effect) {
                is MainEffect.SignIn,
                is MainEffect.SignOut,
                is MainEffect.SignInError,
                is MainEffect.SignOutSuccess,
                is MainEffect.SignInSuccess,
                is MainEffect.ShowToast,
                    -> state

            }
        }
    }

    class MainEventPublisher : Publisher<MainEffect, MainEvent, MainState> {

        override fun publish(effect: MainEffect, currentState: MainState): MainEvent {
            return when (effect) {
                is MainEffect.SignIn -> MainEvent.SignIn
                is MainEffect.SignOut -> MainEvent.SignOut
                is MainEffect.ShowToast -> MainEvent.ShowToast(effect.message)
                is MainEffect.SignInError -> MainEvent.ShowToast(Label.Sign.In.Error.value)
                is MainEffect.SignInSuccess -> MainEvent.ShowToast(Label.Sign.In.Success.value)
                is MainEffect.SignOutSuccess -> MainEvent.ShowToast(Label.Sign.Out.Success.value)
            }
        }
    }
}
    
