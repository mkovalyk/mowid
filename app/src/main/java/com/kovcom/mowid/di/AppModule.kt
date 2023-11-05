package com.kovcom.mowid.di

import androidx.work.WorkManager
import com.kovcom.data.firebase.source.CommonGroupsDataSource
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.preferences.LocalDataSource
import com.kovcom.domain.repository.QuotesRepository
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

@Suppress("RemoveExplicitTypeArguments")
val appModule = module {

    single<WorkManager> {
        WorkManager.getInstance(androidContext())
    }
    single<QuotesWorkerManager> {
        QuotesWorkerManagerImpl(
            get<WorkManager>(), get<LocalDataSource>()
        )
    }
    single<UserProvider> {
        UserProvider(
            get<UserRepository>()
        )
    }
    single<QuotesDataProvider> {
        QuotesDataProvider(
            get<QuotesRepository>()
        )
    }

    viewModel {
        HomeViewModel(
            HomeIntentProcessor(get<QuotesRepository>()),
            HomeReducer(),
            HomePublisher(),
            get<QuotesDataProvider>(),
            get<UserProvider>(),
        )
    }
    viewModel {
        MainViewModel(
            get<QuotesWorkerManager>(),
            get<UserRepository>(),
        )
    }

    viewModel {
        QuotesViewModel(get<QuotesRepository>(), get())
    }

    viewModel {
        SettingsViewModel(
            get<QuotesWorkerManager>(),
            get<QuotesRepository>(),
            get<UserRepository>(),
        )
    }
    worker { params ->
        QuotesWorker(
            androidContext(),
            params.get(),
            get<FirebaseDataSource>(),
            get<CommonGroupsDataSource>(),
            get<LocalDataSource>()
        )
    }
}