package com.kovcom.mowid.base.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
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

    private val initialState: UiState by lazy { createInitialState() }

    private val userIntentQueue: Channel<Intent> = Channel(capacity = DEFAULT_INTENT_CAPACITY)

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val currentState: UiState
        get() = _uiState.value

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    protected val shouldLog = true

    abstract fun tag(): String
    abstract fun createInitialState(): UiState

    private val tag
        get() = tag()

    init {
        coroutineScope.launch {
            userIntentQueue.consumeAsFlow().flatMapLatest { intent ->
                logIntent(intent)
                intentProcessor.processIntent(intent, currentState)
            }.collectLatest { effect ->
                if (shouldLog) Timber.tag(tag).i("Effect: $effect: $currentState")

                val newState = reduce(currentState, effect)
                setState(newState).also {
                    if (shouldLog) Timber.tag(tag).i("Reduce state: $newState")
                }
                withContext(Dispatchers.Main) {
                    val event = publisher.publish(effect, newState)
                    if (shouldLog) Timber.tag(tag).i("Publish State: $newState. $effect -> $event")
                    if (event != null) {
                        _event.emit(event)
                    }
                }
            }
        }

        if (initialUserIntent != null) {
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
        if (shouldLog) Timber.tag(tag).i("reduce[$duration] state: $currentState -> $result")
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

        const val DEFAULT_INTENT_CAPACITY  = 10
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
