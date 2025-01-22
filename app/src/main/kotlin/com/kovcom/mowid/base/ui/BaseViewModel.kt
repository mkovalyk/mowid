package com.kovcom.mowid.base.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.system.measureTimeMillis


@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModel<
        UiState : IState,
        UiEvent : IEvent,
    UiEffect : IEffectV2,
        Intent : UserIntent,
        >(
    private val intentProcessor: IntentProcessor<UiState, Intent, UiEffect>,
    private val reducer: Reducer<UiEffect, UiState>,
    private val publisher: Publisher<UiEffect, UiEvent, UiState>,
    initialUserIntents: List<Intent> = emptyList(),
    dataProviders: List<DataProvider<UiEffect>> = emptyList(),
    initialState: UiState,
) : ViewModel() {

    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val userIntentQueue: Channel<Intent> = Channel(capacity = DEFAULT_INTENT_CAPACITY)

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val currentState: UiState
        get() = _uiState.value

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    val event: Flow<UiEvent> = _event.asSharedFlow()

    protected open val shouldLog = true

    abstract fun tag(): String

    private val tag
        get() = tag()

    private val effectsChannel = Channel<UiEffect>(capacity = MAX_EFFECTS_CAPACITY)

    init {
        coroutineScope.launch {
            userIntentQueue.consumeAsFlow().flatMapLatest { intent ->
                logIntent(intent)

                intentProcessor.processIntent(intent, currentState)
            }.collectLatest {
                Timber.tag(tag).i("Collect Latest -> IEffect: $it")
                effectsChannel.trySend(it)
            }
        }
        coroutineScope.launch {
            effectsChannel.consumeAsFlow().collect { effect ->
                if (shouldLog) Timber.tag(tag).i("IEffect: $effect: $currentState")

                val newState = reduce(currentState, effect)
                setState(newState)
                withContext(Dispatchers.Main) {
                    val event = publisher.publish(effect, newState)
                    if (shouldLog) Timber.tag(tag).i("Publish IState: $effect -> $event")
                    if (event != null) {
                        _event.emit(event)
                    }
                }
            }
        }

        dataProviders.forEach { flow ->
            coroutineScope.launch {
                flow.observe().collectLatest {
                    effectsChannel.send(it)
                }
            }
        }

        initialUserIntents.forEach { initialUserIntent ->
            processIntent(initialUserIntent)
        }
    }

    private fun logIntent(intent: Intent) {
        if (shouldLog) {
            Timber.tag(tag).i("------------------------------------")
            Timber.tag(tag).i("Process intent: $intent")
        }
    }

    private fun reduce(currentState: UiState, effect: UiEffect): UiState {
        var result: UiState
        val duration = measureTimeMillis {
            result = reducer.reduce(effect, currentState)
        }
        return result
    }

    fun processIntent(intent: Intent) {
        // TODO handle highPriority intents
        viewModelScope.launch {
            userIntentQueue.send(intent)
        }
    }

    private fun setState(newState: UiState) {
        _uiState.value = newState
    }

    companion object {

        const val DEFAULT_INTENT_CAPACITY = 10
        const val MAX_EFFECTS_CAPACITY = 15
    }
}

interface DataProvider<E : IEffectV2> {

    fun observe(): Flow<E>
}

interface IntentProcessor<S : IState, Intent : UserIntent, E : IEffectV2> {

    suspend fun processIntent(intent: Intent, currentState: S): Flow<E>
}

interface Reducer<UiEffect : IEffectV2, S : IState> {

    fun reduce(effect: UiEffect, state: S): S
}

interface Publisher<UiEffect : IEffectV2, E : IEvent, S : IState> {

    fun publish(effect: UiEffect, currentState: S): E?
}

const val EVENTS_KEY = "event_key"
