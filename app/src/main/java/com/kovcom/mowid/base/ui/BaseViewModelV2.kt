package com.kovcom.mowid.base.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.system.measureTimeMillis

abstract class BaseViewModelV2<
    UiState : State,
    UiEvent : Event,
    UiEffect : Effect,
    Intent : UserIntent,
    >(
    private val intentProcessor: IntentProcessor<UiState, Intent, UiEffect>,
    private val reducer: Reducer<UiEffect, UiState>,
    private val publisher: Publisher<UiEffect, UiEvent, UiState>,
    initialUserIntent: Intent? = null,
) : ViewModel() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private var eventJob: Job? = null

    abstract fun createInitialState(): UiState

    private val initialState: UiState by lazy { createInitialState() }

    private val userIntentQueue: Channel<Intent> = Channel(capacity = 15)

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val currentState: UiState
        get() = _uiState.value

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val _effect: Channel<UiEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    private val shouldLog = true

    abstract val tag: String

    init {
        coroutineScope.launch {
            userIntentQueue.consumeAsFlow().onEach { intent ->
                val effectFlow = intentProcessor.processIntent(intent, currentState)
                effectFlow.map { effect ->
                    if (shouldLog) Timber.tag(tag).i("intent: $intent -> Effect: $effect")
                    val time = measureTimeMillis {
                        val newState = reducer.reduce(effect, currentState)
                        withContext(Dispatchers.Main) {
                            setState(newState).also {
                                if (shouldLog) Timber.tag(tag).i("Reduce state: $newState")
                            }
                            val event = publisher.publish(effect, currentState)
                            if (shouldLog) Timber.tag(tag).i("publish State: $currentState. $effect -> $event")
                            if (event != null) {
                                _event.emit(event)
                            }
                        }
                    }
                    if (shouldLog) Timber.tag(tag).i("Reduce time Duration: $time")
                }.collect()

            }.collect()
        }

        if (initialUserIntent != null) {
            processIntent(initialUserIntent)
        }
    }

    fun processIntent(intent: Intent) {
        // TODO handle highPriority intents
        viewModelScope.launch {
            userIntentQueue.send(intent)
        }
    }

    private fun setState(proceed: UiState.() -> UiState) {
        val newState = _uiState.value.proceed()
        _uiState.value = newState
    }

    private fun setState(newState: UiState) {
        _uiState.value = newState
    }
}

interface IntentProcessor<S : State, Intent : UserIntent, E : Effect> {

    suspend fun processIntent(intent: Intent, currentState: S): Flow<E>
}

interface Reducer<UiEffect : Effect, S : State> {

    fun reduce(effect: UiEffect, state: S): S
}

interface Publisher<UiEffect : Effect, E : Event, S : State> {

    fun publish(effect: UiEffect, currentState: S): E?
}