package com.kovcom.mowid.di

import com.kovcom.domain.interactor.MotivationPhraseInteractor
import com.kovcom.domain.interactor.UserInteractor
import com.kovcom.domain.repository.MotivationPhraseRepository
import com.kovcom.domain.repository.UserRepository
import org.koin.dsl.module

val domainModule = module {
    factory<MotivationPhraseInteractor> {
        MotivationPhraseInteractor(get<MotivationPhraseRepository>())
    }

    factory<UserInteractor> {
        UserInteractor(get<UserRepository>())
    }
}