@file:Suppress("RemoveExplicitTypeArguments")

package com.kovcom.mowid.ui.feature.settings

import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            IntentProcessor(
                get<QuotesRepository>(),
                get<QuotesWorkerManager>(),
            ),
            Reducer(),
            Publisher(),
            dataProviders = listOf(
                FrequencyDataProvider(get<QuotesRepository>()),
                UserDataProvider(get<UserRepository>()),
            )
        )
    }
}
