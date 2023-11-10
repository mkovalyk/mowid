package com.kovcom.mowid.ui.feature.quotes

import androidx.lifecycle.SavedStateHandle
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl.Companion.TAG
import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.base.ui.IntentProcessor
import com.kovcom.mowid.base.ui.Publisher
import com.kovcom.mowid.base.ui.Reducer
import com.kovcom.mowid.model.toUIModel
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.Effect
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.Event
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.Intent
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.State
import kotlinx.coroutines.flow.*
import timber.log.Timber

class QuotesViewModel(
    savedStateHandle: SavedStateHandle,
    intentProcessor: IntentProcessor<State, Intent, Effect>,
    reducer: Reducer<Effect, State>,
    publisher: Publisher<Effect, Event, State>,
    dataProviders: List<DataProvider<Effect>> = emptyList(),
) : BaseViewModel<State, Event, Effect, Intent>(
    intentProcessor = intentProcessor,
    reducer = reducer,
    publisher = publisher,
    dataProviders = dataProviders,
) {

    override fun tag() = "QuotesViewModel"

    override fun createInitialState() = State()
}

class IntentProcessor(private val quotesRepository: QuotesRepository) : IntentProcessor<State, Intent, Effect> {

    override suspend fun processIntent(
        intent: Intent,
        currentState: State,
    ): Flow<Effect> {
        return when (intent) {
            is Intent.AddQuoteClicked -> flowOf(Effect.ShowQuoteModal)
            is Intent.DeleteQuote -> flowOf(
                Effect.ShowDeleteConfirmationDialog(
                    DeleteDialogInfo(
                        intent.id, intent.isSelected
                    )
                )
            )

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
                        quoteId = intent.id,
                        isSelected = intent.isSelected,
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
                quotesRepository.getGroupsFlow()
                    .map { list -> list.firstOrNull { it.id == intent.id } }
                    .map { Effect.GroupLoaded(it) }
            }

            is Intent.HideDeleteConfirmationDialog -> flowOf(
                Effect.HideDeleteConfirmationDialog(
                    info = DeleteDialogInfo(
                        id = intent.id, isSelected = intent.isSelected
                    )
                )
            )

            is Intent.HideQuoteModal -> flowOf(Effect.HideQuoteModal)
            is Intent.ShowQuoteModal -> flowOf(Effect.ShowQuoteModal)
        }
    }
}

class Reducer : Reducer<Effect, State> {

    override fun reduce(effect: Effect, state: State): State {
        return when (effect) {
            is Effect.Loading -> state.copy(isLoading = effect.isLoading)
            is Effect.QuotesLoaded -> state.copy(quotes = effect.quotes.toUIModel())
            is Effect.ShowDeleteConfirmationDialog -> {
                state.copy(deleteDialogInfo = effect.info,
                           quotes = state.quotes.map {
                               if (it.id == effect.info.id) {
                                   it.copy(isExpanded = true)
                               } else {
                                   it
                               }
                           })
            }

            is Effect.GroupLoaded -> state.copy(group = effect.group)
            is Effect.HideDeleteConfirmationDialog -> state.copy(deleteDialogInfo = null, quotes
            = state.quotes.map {
                if (it.id == effect.info.id) {
                    it.copy(isExpanded = false)
                } else {
                    it
                }
            })

            is Effect.ShowError,
            is Effect.ShowQuote,
            is Effect.ShowQuoteModal,
            is Effect.HideQuoteModal,
            -> state

        }
    }
}

class Publisher : Publisher<Effect, Event, State> {

    override fun publish(effect: Effect, currentState: State): Event? {
        return when (effect) {
            is Effect.ShowQuoteModal -> Event.ShowQuoteModal
            is Effect.ShowError -> Event.ShowError(effect.message)
            is Effect.ShowQuote -> Event.ShowQuote(effect.quote)
            is Effect.HideQuoteModal -> Event.HideQuoteModal

            is Effect.ShowDeleteConfirmationDialog,
            is Effect.GroupLoaded,
            is Effect.QuotesLoaded,
            is Effect.Loading,
            is Effect.HideDeleteConfirmationDialog,
            -> null

        }
    }

}

class QuotesProvider(
    private val groupId: String,
    private val quotesRepository: QuotesRepository,
) : DataProvider<Effect> {

    override fun observe(): Flow<Effect> {
        val quotes: Flow<Effect> = quotesRepository.getQuotes(groupId).flatMapLatest {
            flowOf(
                Effect.QuotesLoaded(it), Effect.Loading(false)
            )
        }
        return quotes.onStart {
            emit(Effect.Loading(true))
        }.catch {
            Timber.tag(TAG).e(it)
            emit(Effect.ShowError(message = it.message.toString()))
        }
    }
}
