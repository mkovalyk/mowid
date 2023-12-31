package com.kovcom.data.repository

import com.kovcom.data.firebase.source.AuthDataSource
import com.kovcom.data.mapper.toDomain
import com.kovcom.data.model.Status
import com.kovcom.domain.model.UserModel
import com.kovcom.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
) : UserRepository {

    override fun getUserFlow(): Flow<UserModel?> {
        return authDataSource.userFlow.map {
            when (it.status) {
                Status.SUCCESS -> it.data?.toDomain()
                Status.ERROR -> throw it.error ?: Exception("Unknown exception")
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
