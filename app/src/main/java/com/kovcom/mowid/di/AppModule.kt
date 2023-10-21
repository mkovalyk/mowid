package com.kovcom.mowid.di

import androidx.work.WorkManager
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.preferences.LocalDataSource
import com.kovcom.domain.interactor.MotivationPhraseInteractor
import com.kovcom.domain.interactor.UserInteractor
import com.kovcom.domain.repository.MotivationPhraseRepository
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.ui.feature.home.*
import com.kovcom.mowid.ui.feature.main.MainViewModel
import com.kovcom.mowid.ui.feature.quotes.QuotesViewModel
import com.kovcom.mowid.ui.feature.settings.SettingsViewModel
import com.kovcom.mowid.ui.worker.QuotesWorker
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import com.kovcom.mowid.ui.worker.QuotesWorkerManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val appModule = module {

    single<WorkManager> {
        WorkManager.getInstance(androidContext())
    }
    single<QuotesWorkerManager> {
        QuotesWorkerManagerImpl(
            get<WorkManager>(), get<LocalDataSource>()
        )
    }
    viewModel {
        HomeViewModel(
            HomeIntentProcessor(
                get<MotivationPhraseRepository>(), get<UserRepository>()
            ),
            HomeReducer(),
            HomePublisher(),
        )
    }
    viewModel {
        MainViewModel(
            get<UserInteractor>(), get<QuotesWorkerManager>()
        )
    }

    viewModel {
        QuotesViewModel(get<MotivationPhraseInteractor>(), get())
    }

    viewModel {
        SettingsViewModel(
            get<QuotesWorkerManager>(),
            get<MotivationPhraseInteractor>(),
            get<UserInteractor>(),
        )
    }
    worker { params ->
        QuotesWorker(
            androidContext(),
            params.get(),
            get<FirebaseDataSource>(),
            get<LocalDataSource>()
        )
    }
}