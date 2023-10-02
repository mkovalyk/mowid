package com.kovcom.data.mapper

import com.kovcom.data.model.UserDataModel
import com.kovcom.domain.model.UserModel

fun UserDataModel.toDomain() = UserModel(
    token = token,
    fullName = fullName.orEmpty(),
    email = email.orEmpty()
)
