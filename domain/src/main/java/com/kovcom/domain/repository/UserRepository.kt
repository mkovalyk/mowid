package com.kovcom.domain.repository

import com.kovcom.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getUserFlow(): Flow<User?>

    fun signInSuccess()

    fun signOutSuccess()
}
