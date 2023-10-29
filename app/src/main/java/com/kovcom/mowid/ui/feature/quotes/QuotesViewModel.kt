package com.kovcom.mowid.ui.feature.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl.Companion.TAG
import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.mowid.base.ui.BaseViewModel
import com.kovcom.mowid.model.toUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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
//                        quote = event.quote,
//                        author = event.author,
                        isSelected = event.checked
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
