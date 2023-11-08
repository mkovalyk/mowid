package com.kovcom.mowid.ui.feature.main

import androidx.core.app.ComponentActivity
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.BaseViewModelV2
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import kotlinx.coroutines.launch

class MainViewModel constructor(
    private val workerManager: QuotesWorkerManager,
    private val userRepository: UserRepository,
) : BaseViewModelV2<MainState, MainEvent, MainEffect, MainUserIntent>() {

    override fun createInitialState(): MainState = MainState.Loading(state = false)

    override fun handleEvent(event: MainEvent) {}

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            userRepository.signInSuccess()
            viewModelScope.launch {
                workerManager.execute(ExecutionOption.Regular)
            }
            MainEffect.ShowToast(R.string.label_sign_in_success).sendEffect()
        } else {
            MainEffect.ShowToast(R.string.label_sign_in_error).sendEffect()
        }
    }

    fun signOutSuccess() {
        userRepository.signOutSuccess()
    }

    fun navigateToQuote(groupId: String, quoteId: String) {
        publishEvent(MainEvent.NavigateToQuote(groupId, quoteId))
    }

}