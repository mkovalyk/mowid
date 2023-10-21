@file:Suppress("RemoveExplicitTypeArguments")

package com.kovcom.data.di

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kovcom.data.di.Const.LOCAL_DATA_STORE
import com.kovcom.data.firebase.source.*
import com.kovcom.data.preferences.LocalDataSource
import com.kovcom.data.preferences.impl.LocalDataSourceImpl
import com.kovcom.data.repository.MotivationPhraseRepositoryImpl
import com.kovcom.data.repository.UserRepositoryImpl
import com.kovcom.domain.repository.MotivationPhraseRepository
import com.kovcom.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val dataModule = module {

    single<CoroutineContext>(named(LOCAL_DATA_STORE)) { Dispatchers.IO }
    single<LocalDataSource> { LocalDataSourceImpl(get<CoroutineContext>(named(LOCAL_DATA_STORE)), androidContext()) }
    single<UserRepository> { UserRepositoryImpl(get<AuthDataSource>()) }
    single<AuthDataSource> { AuthDataSourceImpl(get<FirebaseFirestore>(), get<FirebaseAuth>(), get<LocalDataSource>()) }

    single<FirebaseFirestore> { Firebase.firestore }
    single<FirebaseAuth> { FirebaseAuth.getInstance() }

    single<FirebaseDataSource> {
        FirebaseDataSourceImpl(
            get<FirebaseFirestore>(), get<LocalDataSource>(), get<AuthDataSource>()
        )
    }
    single<CommonGroupsDataSource> {
        CommonGroupsDataSourceImpl(
            get<FirebaseFirestore>(),
            get<LocalDataSource>(),
        )
    }
    single<SharedPreferences> {
        androidContext().getSharedPreferences("TestPrefs", android.content.Context.MODE_PRIVATE)
    }

    single<MotivationPhraseRepository> {
        MotivationPhraseRepositoryImpl(
            get<FirebaseDataSource>(),
            get<CommonGroupsDataSource>()
        )
    }
    single<UserRepository> { UserRepositoryImpl(get<AuthDataSource>()) }
}

object Const {

    const val LOCAL_DATA_STORE = "LocalDataStore"
}
