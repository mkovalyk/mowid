package com.kovcom.data.model

data class Result<out T>(
    val status: Status,
    val data: T? = null,
    val error: Throwable? = null,
) {

    companion object {

        fun <T> success(data: T?): Result<T> {
            return Result(status = Status.Success, data = data)
        }

        fun <T> error(error: Throwable): Result<T> {
            return Result(status = Status.Error, error = error)
        }
    }
}

fun <T> Result<List<T>>.merge(model: Result<List<T>>): Result<List<T>> =
    if (this.status == Status.Success && model.status == Status.Success) {
        Result.success(this.data.orEmpty() + model.data.orEmpty())
    } else {
        Result.error(
            if (this.status == Status.Error) {
                error
            } else {
                model.error
            } ?: Exception("Unknown exception")
        )
    }


enum class Status {
    Success,
    Error
}
