package com.kovcom.data.model


sealed class UserModelBase{
    data class UserModel(
        val token: String = "",
        val id: String = "",
        val fullName: String? = null,
        val email: String? = null,
    ) : UserModelBase()
    data object Empty : UserModelBase()
}

