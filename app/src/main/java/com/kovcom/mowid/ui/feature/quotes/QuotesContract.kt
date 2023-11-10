package com.kovcom.mowid.ui.feature.quotes

import androidx.annotation.StringRes
import com.kovcom.domain.model.Group
import com.kovcom.domain.model.GroupType
import com.kovcom.domain.model.Quote
import com.kovcom.mowid.base.ui.IEffect
import com.kovcom.mowid.base.ui.IEvent
import com.kovcom.mowid.base.ui.IState
import com.kovcom.mowid.base.ui.UserIntent
import com.kovcom.mowid.model.UiQuote

data class QuotesState(
    val isLoading: Boolean,
    val deleteDialogInfo: DeleteDialogInfo? = null,
    val quotes: List<UiQuote>,
) : IState

sealed interface QuotesEvent : IEvent {
    data class QuoteItemChecked(
        val quoteId: String,
        val groupType: GroupType,
        val checked: Boolean,
        val quote: String,
        val author: String?,
    ) : QuotesEvent

    data object ShowQuoteModal : QuotesEvent
    data object HideQuoteModal : QuotesEvent
    data object BackButtonClicked : QuotesEvent
    data class AddQuoteClicked(val quote: String, val author: String) : QuotesEvent
    data class OnItemDeleted(val id: String, val isSelected: Boolean) : QuotesEvent
    data class ShowDeleteConfirmationDialog(val id: String, val isSelected: Boolean) : QuotesEvent
    data class OnEditClicked(
        val id: String,
        val editedQuote: String,
        val editedAuthor: String,
    ) : QuotesEvent

    data object HideDeleteConfirmationDialog : QuotesEvent
}

sealed class QuotesEffect : IEffect {
    data class ShowError(val message: String) : QuotesEffect()
}

object QuotesContract {
    data class State(
        val group: Group? = null,
        val isLoading: Boolean = true,
        val deleteDialogInfo: DeleteDialogInfo? = null,
        val quotes: List<UiQuote> = emptyList(),
    ) : IState

    sealed interface Intent : UserIntent {
        data class LoadGroup(val id: String) : Intent
        data class AddQuoteClicked(val quote: String, val author: String) : Intent
        data object HideQuoteModal : Intent
        data class QuoteChecked(val quote: UiQuote, val groupType: GroupType) : Intent
        data class DeleteQuote(
            val id: String,
            val isSelected: Boolean,
        ) : Intent

        data class QuoteDeletionConfirmed(
            val id: String,
            val isSelected: Boolean,
        ) : Intent

        data class QuoteEditConfirmed(val id: String, val quote: String, val author: String) :
            Intent

        data object HideDeleteConfirmationDialog : Intent
        data object ShowQuoteModal : Intent
    }

    sealed interface Effect : IEffect {
        data class ShowError(val message: String) : Effect
        data class ShowQuote(val quote: UiQuote) : Effect
        data class ShowDeleteConfirmationDialog(val info: DeleteDialogInfo) : Effect
        data object HideDeleteConfirmationDialog : Effect
        data class Loading(val isLoading: Boolean) : Effect
        data class QuotesLoaded(val quotes: List<Quote>) : Effect
        data object ShowQuoteModal : Effect
        data object HideQuoteModal : Effect

        data class GroupLoaded(val group: Group?) : Effect
    }

    sealed interface Event : IEvent {
        data class ShowItemDeleted(val id: String) : Event
        data class ShowErrorRes(@StringRes val resId: Int) : Event
        data class ShowError(val message: String) : Event
        data class ShowQuote(val quote: UiQuote) : Event
        data object ShowQuoteModal: Event
        data object HideQuoteModal: Event
    }
}

data class DeleteDialogInfo(
    val id: String,
    val isSelected: Boolean,
)
    
