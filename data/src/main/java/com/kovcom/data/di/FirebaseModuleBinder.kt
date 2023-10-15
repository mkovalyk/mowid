package com.kovcom.data.di

import com.kovcom.data.firebase.source.AuthDataSource
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.firebase.source.AuthDataSourceImpl
import com.kovcom.data.firebase.source.CommonGroupsDataSource
import com.kovcom.data.firebase.source.CommonGroupsDataSourceImpl
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl
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
    abstract fun bindCommonFirebaseDataSource(
        firebaseDataSourceImpl: CommonGroupsDataSourceImpl,
    ): CommonGroupsDataSource

    @Binds
    abstract fun bindAuthDataSource(
        authDataSource: AuthDataSourceImpl,
    ): AuthDataSource
}
