package com.kovcom.domain.repository

import com.kovcom.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getUserFlow() : Flow<UserModel?>

    fun signInSuccess()

    fun signOutSuccess()
}
