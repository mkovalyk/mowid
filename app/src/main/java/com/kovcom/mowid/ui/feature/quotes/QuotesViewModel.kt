package com.kovcom.mowid.ui.feature.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl.Companion.TAG
import com.kovcom.domain.interactor.MotivationPhraseInteractor
import com.kovcom.mowid.base.ui.BaseViewModel
import com.kovcom.mowid.model.toUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val motivationPhraseInteractor: MotivationPhraseInteractor,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<QuotesState, QuotesEvent, QuotesEffect>() {

    private val groupId = savedStateHandle.get<String>("group_id").orEmpty()

    init {
        motivationPhraseInteractor.getQuotesListFlow(groupId).onStart {
            setState { copy(isLoading = true) }
        }.flowOn(Dispatchers.IO)
            .onEach { data -> setState { copy(isLoading = false, quotes = data.toUIModel()) } }
            .onCompletion { setState { copy(isLoading = false) } }
            .catch {
                Timber.tag(TAG).e(it)
                QuotesEffect.ShowError(
                    message = it.message.toString()
                )
                    .sendEffect()
            }
            .launchIn(viewModelScope)
    }

    private fun deleteQuote(quoteId: String, isSelected: Boolean) {
        viewModelScope.launch {
            motivationPhraseInteractor.deleteQuote(groupId, quoteId, isSelected)
        }
    }

    override fun handleEvent(event: QuotesEvent) {
        when (event) {
            is QuotesEvent.AddQuoteClicked -> {
                viewModelScope.launch {
                    motivationPhraseInteractor.addQuote(
                        groupId = groupId, quote = event.quote, author = event.author
                    )
                }
            }

            QuotesEvent.HideQuoteModal -> {}
            is QuotesEvent.QuoteItemChecked -> {
                viewModelScope.launch {
                    motivationPhraseInteractor.saveSelection(
                        groupId = groupId,
                        quoteId = event.quoteId,
                        quote = event.quote,
                        author = event.author,
                        isSelected = event.checked
                    )
                }
            }

            QuotesEvent.ShowQuoteModal -> {}
            QuotesEvent.BackButtonClicked -> {}
            is QuotesEvent.OnItemDeleted -> deleteQuote(event.id, event.isSelected)
            is QuotesEvent.OnEditClicked -> {
                viewModelScope.launch {
                    motivationPhraseInteractor.editQuote(
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
