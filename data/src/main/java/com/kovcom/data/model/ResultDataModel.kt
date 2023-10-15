package com.kovcom.data.model

data class ResultDataModel<out T>(
    val status: Status,
    val data: T? = null,
    val error: Throwable? = null
) {

    companion object {
        fun <T> success(data: T?): ResultDataModel<T> {
            return ResultDataModel(status = Status.Success, data = data)
        }

        fun <T> error(error: Throwable): ResultDataModel<T> {
            return ResultDataModel(status = Status.Error, error = error)
        }
    }
}

fun <T> ResultDataModel<List<T>>.merge(model: ResultDataModel<List<T>>): ResultDataModel<List<T>> =
    if (this.status == Status.Success && model.status == Status.Success) {
        ResultDataModel.success(this.data.orEmpty() + model.data.orEmpty())
    } else {
        ResultDataModel.error(
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
