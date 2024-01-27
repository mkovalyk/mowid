@file:Suppress("RemoveExplicitTypeArguments")

package com.kovcom.mowid.ui.feature.quotes

import com.kovcom.domain.repository.QuotesRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val quotesModule = module {
    viewModel<QuotesViewModel> { params ->
        val groupId: String = params[0]
        QuotesViewModel(
            IntentProcessor(get<QuotesRepository>()),
            Reducer(),
            Publisher(),
            initialState = QuotesContract.State(groupId = groupId),
            dataProviders = listOf(QuotesProvider(groupId, get<QuotesRepository>()))
        )
    }
}
