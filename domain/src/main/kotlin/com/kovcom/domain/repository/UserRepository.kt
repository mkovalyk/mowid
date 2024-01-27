package com.kovcom.domain.repository

import com.kovcom.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val userFlow: Flow<User?>

    fun signInSuccess()

    fun signOutSuccess()
}
