package com.kovcom.mowid.ui.feature.main

import androidx.core.app.ComponentActivity
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.kovcom.domain.interactor.UserInteractor
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.BaseViewModel
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val interactor: UserInteractor,
    private val workerManager: QuotesWorkerManager,
) : BaseViewModel<MainState, MainEvent, MainEffect>() {

    override fun createInitialState(): MainState = MainState.Loading(state = false)

    override fun handleEvent(event: MainEvent) {}

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            interactor.signInSuccess()
            viewModelScope.launch {
                workerManager.execute(ExecutionOption.REGULAR)
            }
            MainEffect.ShowToast(R.string.label_sign_in_success).sendEffect()
        } else {
            MainEffect.ShowToast(R.string.label_sign_in_error).sendEffect()
        }
    }

    fun signOutSuccess() {
        interactor.signOutSuccess()
    }

    fun navigateToQuote(groupId: String, quoteId: String) {
        publishEvent(MainEvent.NavigateToQuote(groupId, quoteId))
    }

}