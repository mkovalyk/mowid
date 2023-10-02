package com.kovcom.mowid.di

import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ActionCallBackEntryPoint {
    fun workManager(): QuotesWorkerManager
}
