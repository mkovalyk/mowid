package com.kovcom.data.mapper

import com.kovcom.data.model.UserModel
import com.kovcom.domain.model.User

fun UserModel.toDomain() = User(
    token = token,
    fullName = fullName.orEmpty(),
    email = email.orEmpty()
)
