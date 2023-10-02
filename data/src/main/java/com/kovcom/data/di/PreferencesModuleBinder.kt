package com.kovcom.data.di

import android.content.Context
import com.kovcom.data.preferences.LocalDataSource
import com.kovcom.data.preferences.impl.LocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModuleBinder {

    @Binds
    abstract fun bindLocalDataSource(
        localDataSourceImpl: LocalDataSourceImpl,
    ): LocalDataSource

    @Binds
    abstract fun bindContext(
        @ApplicationContext context: Context,
    ): Context
}