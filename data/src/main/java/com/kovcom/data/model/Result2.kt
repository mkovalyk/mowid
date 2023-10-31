package com.kovcom.data.model

sealed class Result2<out T> {
    data class Success<T>(val value: T) : Result2<T>()
    data class Error<T>(val error: Throwable) : Result2<T>()

    val data: T?
        get() = when (this) {
            is Success -> value
            is Error -> null
        }
}