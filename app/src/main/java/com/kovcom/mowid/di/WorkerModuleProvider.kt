package com.kovcom.mowid.di

import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import com.kovcom.mowid.ui.worker.QuotesWorkerManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModuleProvider {

    @Binds
    internal abstract fun bindWorker(workManager: QuotesWorkerManagerImpl): QuotesWorkerManager
}
