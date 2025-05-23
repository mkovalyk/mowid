package com.kovcom.mowid.di

import androidx.work.WorkManager
import com.kovcom.data.firebase.source.CommonGroupsDataSource
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.preferences.LocalDataSource
import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.ui.feature.home.*
import com.kovcom.mowid.ui.feature.main.MainViewModel
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
            MainViewModel.MainIntentProcessor(
                get<UserRepository>(),
                get<QuotesWorkerManager>()
            ),
            MainViewModel.MainEventReducer(),
            MainViewModel.MainEventPublisher()
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
