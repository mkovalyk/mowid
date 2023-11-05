package com.kovcom.data.firebase.source

import com.kovcom.data.model.UserModelBase
import kotlinx.coroutines.flow.Flow
import com.kovcom.data.model.Result

interface AuthDataSource {

    fun signInSuccess()

    fun signOutSuccess()

    val userFlow: Flow<Result<UserModelBase>>
}
