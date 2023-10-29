package com.kovcom.data.mapper

import com.kovcom.data.model.UserDataModel
import com.kovcom.domain.model.User

fun UserDataModel.toDomain() = User(
    token = token,
    fullName = fullName.orEmpty(),
    email = email.orEmpty()
)
