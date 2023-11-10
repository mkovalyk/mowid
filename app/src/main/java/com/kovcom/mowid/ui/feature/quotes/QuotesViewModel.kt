package com.kovcom.mowid.ui.feature.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl.Companion.TAG
import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.mowid.base.ui.BaseViewModel
import com.kovcom.mowid.base.ui.BaseViewModelV2
import com.kovcom.mowid.base.ui.DataProvider
import com.kovcom.mowid.base.ui.IntentProcessor
import com.kovcom.mowid.base.ui.Publisher
import com.kovcom.mowid.base.ui.Reducer
import com.kovcom.mowid.model.toUIModel
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.Effect
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.Event
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.Intent
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class QuotesViewModel constructor(
    private val quotesRepository: QuotesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<QuotesState, QuotesEvent, QuotesEffect>() {

    private val groupId = savedStateHandle.get<String>("group_id").orEmpty()

    init {
        quotesRepository.getQuotes(groupId).onStart {
            setState { copy(isLoading = true) }
        }.flowOn(Dispatchers.IO)
            .onEach { data -> setState { copy(isLoading = false, quotes = data.toUIModel()) } }
            .onCompletion { setState { copy(isLoading = false) } }
            .catch {
                Timber.tag(TAG).e(it)
                QuotesEffect.ShowError(message = it.message.toString())
                    .sendEffect()
            }
            .launchIn(viewModelScope)
    }

    private fun deleteQuote(quoteId: String, isSelected: Boolean) {
        viewModelScope.launch {
            quotesRepository.deleteQuote(groupId, quoteId, isSelected)
        }
    }

    override fun handleEvent(event: QuotesEvent) {
        when (event) {
            is QuotesEvent.AddQuoteClicked -> {

                val quoteId = UUID.randomUUID().toString()
                viewModelScope.launch {
                    quotesRepository.addQuote(
                        groupId = groupId,
                        quote = event.quote,
                        author = event.author,
                        quoteId = quoteId,
                    )
                }
            }

            QuotesEvent.HideQuoteModal -> {}
            is QuotesEvent.QuoteItemChecked -> {
                viewModelScope.launch {
                    quotesRepository.saveSelection(
                        groupId = groupId,
                        quoteId = event.quoteId,
                        isSelected = event.checked,
                        groupType = event.groupType
                    )
                }
            }

            QuotesEvent.ShowQuoteModal -> {}
            QuotesEvent.BackButtonClicked -> {}
            is QuotesEvent.OnItemDeleted -> deleteQuote(event.id, event.isSelected)
            is QuotesEvent.OnEditClicked -> {
                viewModelScope.launch {
                    quotesRepository.editQuote(
                        groupId = groupId,
                        quoteId = event.id,
                        editedQuote = event.editedQuote,
                        editedAuthor = event.editedAuthor
                    )
                }
            }

            is QuotesEvent.ShowDeleteConfirmationDialog -> {
                setState {
                    copy(
                        deleteDialogInfo = DeleteDialogInfo(
                            id = event.id, isSelected = event.isSelected
                        )
                    )
                }
            }

            QuotesEvent.HideDeleteConfirmationDialog -> {
                setState { copy(deleteDialogInfo = null) }
            }
        }
    }

    override fun createInitialState(): QuotesState = QuotesState(
        isLoading = true, deleteDialogInfo = null, quotes = emptyList()
    )
}

class QuotesViewModel2(
    savedStateHandle: SavedStateHandle,
    intentProcessor: IntentProcessor<State, Intent, Effect>,
    reducer: Reducer<Effect, State>,
    publisher: Publisher<Effect, Event, State>,
    dataProviders: List<DataProvider<Effect>> = emptyList(),
) : BaseViewModelV2<State, Event, Effect, Intent>(
    intentProcessor = intentProcessor,
    reducer = reducer,
    publisher = publisher,
    dataProviders = dataProviders,
) {

    private val groupId = savedStateHandle.get<String>("group_id").orEmpty()

    override fun tag() = "QuotesViewModel"

    override fun createInitialState() = State()
}

class IntentProcessor(private val quotesRepository: QuotesRepository) :
    IntentProcessor<State, Intent, Effect> {

    override suspend fun processIntent(
        intent: Intent,
        currentState: State,
    ): Flow<Effect> {
        return when (intent) {
            is Intent.AddQuoteClicked -> flowOf(Effect.ShowAddQuoteModal)
            is Intent.DeleteQuote -> flowOf(Effect.ShowDeleteConfirmationDialog(intent.quote))
            is Intent.QuoteChecked -> {
                currentState.group?.let {
                    quotesRepository.saveSelection(
                        groupId = currentState.group.id,
                        quoteId = intent.quote.id,
                        isSelected = intent.quote.isSelected,
                        groupType = currentState.group.groupType,
                    )
                }
                emptyFlow()
            }

            is Intent.QuoteDeletionConfirmed -> {
                currentState.group?.let {
                    quotesRepository.deleteQuote(
                        groupId = currentState.group.id,
                        quoteId = intent.quote.id,
                        isSelected = intent.quote.isSelected,
                    )
                }
                emptyFlow()
            }

            is Intent.QuoteEditConfirmed -> flow {
                emit(Effect.Loading(true))
                currentState.group?.let {
                    quotesRepository.editQuote(
                        groupId = currentState.group.id,
                        quoteId = intent.id,
                        editedQuote = intent.quote,
                        editedAuthor = intent.author,
                    )
                }
                emit(Effect.Loading(false))
            }

            is Intent.LoadGroup -> {
                quotesRepository.getGroupsFlow().map { it.firstOrNull { it.id == intent.id } }
                    .map { Effect.GroupLoaded(it) }
            }
        }
    }
}

class Reducer : Reducer<Effect, State> {

    override fun reduce(effect: Effect, state: State): State {
        return when (effect) {
            is Effect.Loading -> state.copy(isLoading = effect.isLoading)
            is Effect.QuotesLoaded -> state.copy(quotes = effect.quotes.toUIModel())
            is Effect.ShowDeleteConfirmationDialog -> state.copy(
                deleteDialogInfo = DeleteDialogInfo(
                    id = effect.quote.id,
                    isSelected = effect.quote.isSelected
                )
            )

            is Effect.GroupLoaded -> state.copy(group = effect.group)

            is Effect.ShowError,
            is Effect.ShowQuote,
            is Effect.ShowAddQuoteModal,
            -> state

        }
    }
}

class Publisher : Publisher<Effect, Event, State> {

    override fun publish(effect: Effect, currentState: State): Event? {
        return when (effect) {
            is Effect.ShowError -> Event.ShowError(effect.message)
            is Effect.ShowQuote -> Event.ShowQuote(effect.quote)
            is Effect.ShowDeleteConfirmationDialog,
            is Effect.GroupLoaded,
            is Effect.QuotesLoaded,
            is Effect.Loading,
            -> null

            Effect.ShowAddQuoteModal -> Event.ShowAddQuoteModal
        }
    }

}

class QuotesProvider(
    private val groupId: String,
    private val quotesRepository: QuotesRepository,
) : DataProvider<Effect> {

    override fun observe(): Flow<Effect> {
        val quotes: Flow<Effect> =
            quotesRepository.getQuotes(groupId).flatMapLatest {
                flowOf(
                    Effect.QuotesLoaded(it),
                    Effect.Loading(false)
                )
            }
        return quotes
            .onStart {
                emit(Effect.Loading(true))
            }
            .catch {
                Timber.tag(TAG).e(it)
                emit(Effect.ShowError(message = it.message.toString()))
            }
    }
}
