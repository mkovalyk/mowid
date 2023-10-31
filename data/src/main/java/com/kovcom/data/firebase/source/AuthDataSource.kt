package com.kovcom.data.firebase.source

import com.kovcom.data.model.Result2
import com.kovcom.data.model.UserModel
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {

    fun signInSuccess()

    fun signOutSuccess()

    val userFlow: Flow<Result2<UserModel>>
}
