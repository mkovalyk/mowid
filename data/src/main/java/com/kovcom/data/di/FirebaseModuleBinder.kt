package com.kovcom.data.di

import com.kovcom.data.firebase.source.AuthDataSource
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.firebase.source.impl.AuthDataSourceImpl
import com.kovcom.data.firebase.source.impl.FirebaseDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModuleBinder {

    @Binds
    abstract fun bindFirebaseDataSource(
        firebaseDataSourceImpl: FirebaseDataSourceImpl,
    ): FirebaseDataSource

    @Binds
    abstract fun bindAuthDataSource(
        authDataSource: AuthDataSourceImpl,
    ): AuthDataSource


}
