package com.kovcom.data.repository

import com.kovcom.data.firebase.source.AuthDataSource
import com.kovcom.data.mapper.toDomain
import com.kovcom.data.model.Result2
import com.kovcom.data.model.UserModelBase
import com.kovcom.domain.repository.UserRepository
import kotlinx.coroutines.flow.map


class UserRepositoryImpl constructor(
    private val authDataSource: AuthDataSource,
) : UserRepository {

    override val userFlow = authDataSource.userFlow.map {
        when (it) {
            is Result2.Success -> when (val data = it.data) {
                is UserModelBase.UserModel -> data.toDomain()

                is UserModelBase.Empty,
                null,
                -> null
            }

            is Result2.Error -> throw it.error
        }
    }

    override fun signInSuccess() {
        authDataSource.signInSuccess()
    }

    override fun signOutSuccess() {
        authDataSource.signOutSuccess()
    }
}
