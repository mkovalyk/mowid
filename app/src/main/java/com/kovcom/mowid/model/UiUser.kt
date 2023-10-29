package com.kovcom.mowid.model

import com.kovcom.domain.model.User

data class UiUser(
    val token: String,
    val fullName: String,
    val email: String,
)

fun User.toUIModel() = UiUser(
    token = token,
    fullName = fullName,
    email = email
)