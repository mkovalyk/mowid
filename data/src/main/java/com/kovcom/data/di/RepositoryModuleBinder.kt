package com.kovcom.data.di

import com.kovcom.data.repository.MotivationPhraseRepositoryImpl
import com.kovcom.domain.repository.MotivationPhraseRepository
import com.kovcom.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModuleBinder {

    @Binds
    abstract fun bindTestRepository(
        motivationPhraseRepository: MotivationPhraseRepositoryImpl
    ): MotivationPhraseRepository

    @Binds
    abstract fun bindUserRepository(
        userRepository: UserRepository
    ): UserRepository
}