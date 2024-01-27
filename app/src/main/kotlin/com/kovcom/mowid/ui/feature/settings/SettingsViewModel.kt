package com.kovcom.mowid.ui.feature.settings

import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.Label
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.base.ui.IntentProcessor
import com.kovcom.mowid.base.ui.Publisher
import com.kovcom.mowid.base.ui.Reducer
import com.kovcom.mowid.model.toUIModel
import com.kovcom.mowid.ui.feature.settings.SettingsContract.Effect
import com.kovcom.mowid.ui.feature.settings.SettingsContract.Event
import com.kovcom.mowid.ui.feature.settings.SettingsContract.Intent
import com.kovcom.mowid.ui.feature.settings.SettingsContract.State
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import kotlinx.coroutines.flow.*

class SettingsViewModel(
    intentProcessor: IntentProcessor<State, Intent, Effect>,
    reducer: Reducer<Effect, State>,
    publisher: Publisher<Effect, Event, State>,
    dataProviders: List<DataProvider<Effect>>,
) : BaseViewModel<State, Event, Effect, Intent>(
    intentProcessor, reducer, publisher,
    dataProviders = dataProviders,
    initialState = State(),
) {

    override fun tag(): String = "SettingsViewModel"
}

class IntentProcessor(
    private val repository: QuotesRepository,
    private val quotesWorkerManager: QuotesWorkerManager,
) : IntentProcessor<State, Intent, Effect> {

    override suspend fun processIntent(
        intent: Intent,
        currentState: State,
    ): Flow<Effect> {
        return when (intent) {
            is Intent.FrequencyChanged -> {
                flow {
                    repository.updateUserFrequency(intent.id)
                    quotesWorkerManager.execute(ExecutionOption.Regular)
                    emit(Effect.FrequencyChanged(intent.id))
                }
            }
        }
    }
}

class FrequencyDataProvider(
    private val repository: QuotesRepository,
) : DataProvider<Effect> {

    override fun observe(): Flow<Effect> {
        return repository.getFrequencySettingsFlow()
            .flatMapLatest {
                flowOf(Effect.Loading(false), Effect.FrequenciesLoaded(it.frequencies, it.selectedFrequency))
            }
            .onStart { emit(Effect.Loading(true)) }
            .catch {
                emit(Effect.Error(it.message.toString()))
            }
    }
}

class UserDataProvider(
    private val repository: UserRepository,
) : DataProvider<Effect> {

    override fun observe(): Flow<Effect> {
        return repository.userFlow.flatMapLatest {
            flowOf(Effect.Loading(false), Effect.UserLoaded(it))
        }
            .onStart { emit(Effect.Loading(true)) }
            .catch {
                emit(Effect.Error(it.message.toString()))
            }
    }
}

class Reducer : Reducer<Effect, State> {

    override fun reduce(effect: Effect, state: State): State {
        return when (effect) {
            is Effect.Loading -> state.copy(isLoading = effect.isLoading)
            is Effect.FrequenciesLoaded -> state.copy(
                frequencies = effect.frequencies.toUIModel(),
                selectedFrequency = effect.selectedFrequency?.toUIModel()
            )

            is Effect.UserLoaded -> state.copy(userModel = effect.user?.toUIModel())
            is Effect.FrequencyChanged -> state.copy(selectedFrequency = state.frequencies
                .find { it.frequencyId == effect.id })

            is Effect.Error -> state
        }
    }
}

class Publisher : Publisher<Effect, Event, State> {

    override fun publish(effect: Effect, currentState: State): Event? {
        return when (effect) {
            is Effect.FrequenciesLoaded,
            is Effect.UserLoaded,
            is Effect.Loading,
            -> null

            is Effect.FrequencyChanged -> Event.ShowToast(Label.Applied.value)
            is Effect.Error -> Event.ShowToast(effect.message)
        }
    }
}
