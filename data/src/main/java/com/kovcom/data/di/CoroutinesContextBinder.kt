package com.kovcom.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
class CoroutinesContextBinder {

    @Provides
    @Named("LocalDataStore")
    fun provideCoroutineDispatcherProvider(): CoroutineContext {
        return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }
}