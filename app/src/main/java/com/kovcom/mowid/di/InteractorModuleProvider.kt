package com.kovcom.mowid.di

import com.kovcom.data.repository.MotivationPhraseRepositoryImpl
import com.kovcom.data.repository.UserRepositoryImpl
import com.kovcom.domain.interactor.MotivationPhraseInteractor
import com.kovcom.domain.interactor.UserInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object InteractorModuleProvider {

    @Provides
    fun provideMotivationPhraseInteractor(
        motivationPhraseRepository: MotivationPhraseRepositoryImpl
    ): MotivationPhraseInteractor {
        return MotivationPhraseInteractor(motivationPhraseRepository)
    }

    @Provides
    fun provideUserInteractor(
        userRepository: UserRepositoryImpl
    ): UserInteractor {
        return UserInteractor(userRepository)
    }
}