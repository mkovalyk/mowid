package com.kovcom.mowid.ui.worker

import androidx.work.*
import com.kovcom.data.preferences.LocalDataSource
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QuotesWorkerManagerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val localDataSource: LocalDataSource
) : QuotesWorkerManager {

    override suspend fun execute(option: ExecutionOption) = enqueueWorker(option)

    private suspend fun enqueueWorker(option: ExecutionOption) {
        workManager.enqueueUniquePeriodicWork(
            QuotesWorker.TAG,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            buildRequest(option)
        )
    }

    private suspend fun buildRequest(option: ExecutionOption): PeriodicWorkRequest {
        saveOption(option)
        val frequency = localDataSource.frequency.first()
        return PeriodicWorkRequestBuilder<QuotesWorker>(frequency, TimeUnit.HOURS)
            .addTag(QuotesWorker.TAG)
            .setConstraints(getDRMConstraints())
            .build()
    }

    private fun saveOption(option: ExecutionOption) {
        localDataSource.setQuoteChangeOption(option.name)
    }

    companion object {
        private fun getDRMConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        }
    }
}
