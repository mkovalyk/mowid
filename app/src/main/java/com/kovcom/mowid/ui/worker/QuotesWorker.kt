package com.kovcom.mowid.ui.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.model.SelectedQuoteDataModel
import com.kovcom.data.model.Status
import com.kovcom.data.preferences.LocalDataSource
import com.kovcom.mowid.ui.feature.widget.QuotesWidgetReceiver
import com.kovcom.mowid.ui.feature.widget.WidgetQuoteInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date

class QuotesWorker constructor(
    private val context: Context,
    workParams: WorkerParameters,
    private val firebaseDataSource: FirebaseDataSource,
    private val localDataSource: LocalDataSource,
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {
        return localDataSource.quoteChangeOption.map { quoteOption ->

            val option = ExecutionOption.valueOf(
                quoteOption ?: ExecutionOption.Regular.name
            )
            Timber.tag("QuotesWorker").i("doWork option = ${option.name}")
            val result = firebaseDataSource.getSelectedQuotes()
            when (result.status) {
                Status.Success -> {
                    showNextQuote(result.data, option)
                    Result.success()
                }

                Status.Error -> Result.failure()
            }
        }.first()

    }

    private suspend fun showNextQuote(quotes: List<SelectedQuoteDataModel>?, option: ExecutionOption) {
        quotes?.let {
            when (option) {
                ExecutionOption.Regular -> showRegularQuote(it)
                ExecutionOption.Next -> showNextQuote(it)
                ExecutionOption.Previous -> showPreviousQuote(it)
            }
        }
    }

    private fun SelectedQuoteDataModel.toWidgetInfo(): WidgetQuoteInfo {
        return WidgetQuoteInfo(
            quote = quote ?: "",
            author = author ?: "",
            quoteId = id,
            groupId = groupId
        )
    }

    private suspend fun showRegularQuote(quotes: List<SelectedQuoteDataModel>) {
        quotes.sortedBy { it.shownAt }.firstOrNull()?.let {
            QuotesWidgetReceiver.updateWidget(
                context = context,
                info = it.toWidgetInfo()
            )
            updateShownQuote(it)
        }
    }

    private suspend fun showNextQuote(quotes: List<SelectedQuoteDataModel>) {
        val currentQuote = quotes.sortedBy { it.shownAt }.lastOrNull()
        val currentQuoteIndex = quotes.indexOf(currentQuote)
        val nextQuote = when {
            currentQuoteIndex == quotes.lastIndex -> quotes.first()
            currentQuoteIndex >= 0 -> quotes[currentQuoteIndex + 1]
            else -> null
        }
        nextQuote?.let {
            QuotesWidgetReceiver.updateWidget(
                context = context,
                info = it.toWidgetInfo()
            )
            updateShownQuote(it)
        }
        localDataSource.setQuoteChangeOption(ExecutionOption.Regular.name)
    }

    private suspend fun showPreviousQuote(quotes: List<SelectedQuoteDataModel>) {
        val currentQuote = quotes.sortedBy { it.shownAt }.lastOrNull()
        val currentQuoteIndex = quotes.indexOf(currentQuote)
        val previousQuote = when {
            currentQuoteIndex == 0 -> quotes.last()
            currentQuoteIndex > 0 -> quotes[currentQuoteIndex - 1]
            else -> null
        }
        previousQuote?.let {
            QuotesWidgetReceiver.updateWidget(
                context = context,
                info = it.toWidgetInfo()
            )
            updateShownQuote(it)
        }
        localDataSource.setQuoteChangeOption(ExecutionOption.Regular.name)
    }

    private suspend fun updateShownQuote(quote: SelectedQuoteDataModel) {
        firebaseDataSource.updateSelectedQuote(
            groupId = quote.groupId,
            quoteId = quote.id,
            shownTime = Date().time
        )
    }

    companion object {

        const val TAG = "quotes-worker"
    }
}
