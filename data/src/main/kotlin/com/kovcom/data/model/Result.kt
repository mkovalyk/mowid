package com.kovcom.data.model

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Error<T>(val throwable: Throwable) : Result<T>()

    val data: T?
        get() = when (this) {
            is Success -> value
            is Error -> null
        }
    
    val error: Throwable?
        get() = when (this) {
            is Error -> throwable
            is Success -> null
        }

    companion object {

        fun <T> success(data: T): Result<T> {
            return Success(data)
        }

        fun <T> error(ex: Throwable): Result<T> {
            return Error(ex)
        }
    }
}

fun <T> Result<List<T>>.merge(model: Result<List<T>>): Result<List<T>> =
    if (this is Result.Success && model is Result.Success) {
        Result.success(this.data.orEmpty() + model.data.orEmpty())
    } else {
        Result.error(
            if (this is Result.Error) {
                throwable
            } else if (model is Result.Error) {
                model.throwable
            } else {
                Exception("Unknown exception")
            }
        )
    }
