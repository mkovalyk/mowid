package com.kovcom.mowid.ui.feature.settings

import androidx.lifecycle.viewModelScope
import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.BaseViewModel
import com.kovcom.mowid.model.toUIModel
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val quotesWorkerManager: QuotesWorkerManager,
    private val repository: QuotesRepository,
    private val userRepository: UserRepository,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsEffect>() {

    init {
        repository.getFrequencySettingsFlow()
            .combine(userRepository.userFlow) { frequency, user ->
                frequency to user
            }
            .onStart {
                setState { copy(isLoading = true) }
            }
            .flowOn(Dispatchers.IO)
            .onEach { data ->
                setState {
                    copy(
                        isLoading = false,
                        selectedFrequency = data.first.selectedFrequency?.toUIModel(),
                        frequencies = data.first.frequencies.toUIModel().sortedBy { it.frequencyId },
                        userModel = data.second?.toUIModel()
                    )
                }
            }
            .onCompletion {
                setState { copy(isLoading = false) }
            }
            .catch {
                SettingsEffect.ShowToast(
                    message = it.message.toString()
                ).sendEffect()
            }
            .launchIn(viewModelScope)
    }

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnFrequencyChanged -> {
                viewModelScope.launch {
                    repository.updateUserFrequency(event.id)
                    quotesWorkerManager.execute(ExecutionOption.Regular)
                    SettingsEffect.ShowToastId(
                        messageId = R.string.label_applied
                    ).sendEffect()
                }
            }

            SettingsEvent.BackButtonClicked -> {}
        }
    }

    override fun createInitialState(): SettingsState = SettingsState(
        isLoading = true,
        selectedFrequency = null,
        frequencies = emptyList(),
        userModel = null,
    )
}
