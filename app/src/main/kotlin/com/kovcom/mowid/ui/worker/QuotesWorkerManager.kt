package com.kovcom.mowid.ui.worker

interface QuotesWorkerManager {

    suspend fun execute(option: ExecutionOption)
}

enum class ExecutionOption {
    Regular,
    Previous,
    Next
}
