@file:Suppress("RemoveExplicitTypeArguments")

package com.kovcom.mowid.ui.feature.quotes

import com.kovcom.domain.repository.QuotesRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val quotesModule = module {
    viewModel<QuotesViewModel> { params -> 
        QuotesViewModel(
            get(),
            IntentProcessor(get<QuotesRepository>()),
            Reducer(),
            Publisher(),
            dataProviders = listOf(QuotesProvider(params[0], get<QuotesRepository>()))
        )
    }
}
