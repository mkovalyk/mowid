package com.kovcom.data.mapper

import com.kovcom.data.model.UserModelBase
import com.kovcom.domain.model.User

fun UserModelBase.UserModel.toDomain() = User(
    token = token,
    fullName = fullName.orEmpty(),
    email = email.orEmpty()
)
