package com.kovcom.mowid.model

import com.kovcom.domain.model.UserModel

data class UserUIModel(
    val token: String,
    val fullName: String,
    val email: String,
)

fun UserModel.toUIModel() = UserUIModel(
    token = token,
    fullName = fullName,
    email = email
)