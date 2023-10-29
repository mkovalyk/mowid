package com.kovcom.data.repository

import com.kovcom.data.firebase.source.AuthDataSource
import com.kovcom.data.mapper.toDomain
import com.kovcom.data.model.Status
import com.kovcom.domain.model.User
import com.kovcom.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UserRepositoryImpl  constructor(
    private val authDataSource: AuthDataSource,
) : UserRepository {

    override fun getUserFlow(): Flow<User?> {
        return authDataSource.userFlow.map {
            when (it.status) {
                Status.Success -> it.data?.toDomain()
                Status.Error -> throw it.error ?: Exception("Unknown exception")
            }
        }
    }

    override fun signInSuccess() {
        authDataSource.signInSuccess()
    }

    override fun signOutSuccess() {
        authDataSource.signOutSuccess()
    }
}
