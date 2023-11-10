package com.kovcom.mowid.ui.feature.main

import androidx.core.app.ComponentActivity
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.BaseViewModel
import com.kovcom.mowid.base.ui.IntentProcessor
import com.kovcom.mowid.base.ui.Publisher
import com.kovcom.mowid.base.ui.Reducer
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class MainViewModel constructor(
    intentProcessor: IntentProcessor<MainState, MainUserIntent, MainEffect>,
    reducer: Reducer<MainEffect, MainState>,
    publisher: Publisher<MainEffect, MainEvent, MainState>,
) : BaseViewModel<MainState, MainEvent, MainEffect, MainUserIntent>(
    intentProcessor, reducer,
    publisher
) {

    override fun tag() = "MainViewModel"

    override fun createInitialState(): MainState = MainState.Loading

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

        private suspend fun signInResult(result: FirebaseAuthUIAuthenticationResult): Flow<MainEffect> {
            return flow {
                if (result.resultCode == ComponentActivity.RESULT_OK) {
                    userRepository.signInSuccess()
                    workerManager.execute(ExecutionOption.Regular)
                    flowOf(MainEffect.ShowToast(R.string.label_sign_in_success))
                } else {
                    flowOf(MainEffect.ShowToast(R.string.label_sign_in_error))
                }
            }
        }
    }

    class MainEventReducer: Reducer<MainEffect, MainState> {

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
                is MainEffect.ShowToast -> MainEvent.ShowToast(effect.messageId)
                is MainEffect.SignInError -> MainEvent.ShowToast(R.string.label_sign_in_error)
                is MainEffect.SignInSuccess -> MainEvent.ShowToast(R.string.label_sign_in_success)
                is MainEffect.SignOutSuccess -> MainEvent.ShowToast(R.string.label_sign_out_success)
            }
        }
    }
}
    
