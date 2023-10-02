package com.kovcom.data.firebase.source

import com.kovcom.data.model.ResultDataModel
import com.kovcom.data.model.UserDataModel
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {

    fun signInSuccess()

    fun signOutSuccess()

    val userFlow: Flow<ResultDataModel<UserDataModel>>
}
