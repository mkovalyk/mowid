package com.kovcom.mowid.ui.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kovcom.data.firebase.source.FirebaseDataSource
import com.kovcom.data.model.SelectedQuoteModel
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
            val result = firebaseDataSource.getSelectedQuotes()
            Timber.tag("QuotesWorker").i("doWork option = ${option.name}. Result: $result")
            when (result) {
                is com.kovcom.data.model.Result.Success -> {
                    showNextQuote(result.data, option)
                    Result.success()
                }

                is com.kovcom.data.model.Result.Error -> Result.failure()
            }
        }.first()

    }

    private suspend fun showNextQuote(quotes: List<SelectedQuoteModel>?, option: ExecutionOption) {
        quotes?.let {
            val item = it.firstOrNull()
            if (item == null) {
                Timber.tag("QuotesWorker").i("showNextQuote: item is null")
                return
            }
            when (option) {
                ExecutionOption.Regular -> showRegularQuote(it)
                ExecutionOption.Next -> showNextQuote(it)
                ExecutionOption.Previous -> showPreviousQuote(it)
            }
        }
    }

    private fun SelectedQuoteModel.toWidgetInfo(): WidgetQuoteInfo {
        return WidgetQuoteInfo(
            quote = quote ?: "Default",
            author = author ?: "Author",
            quoteId = id,
            groupId = groupId
        ).also {
            Timber.tag("QuotesWorker").i("toWidgetInfo: $it")
        }
    }

    private suspend fun getAndUpdateWidgetInfo(item: SelectedQuoteModel) {
        firebaseDataSource.getQuoteById(item.groupId, item.groupType, item.id).let { result ->

            val updated = item.copy(
                quote = result.data?.quote,
                author = result.data?.author
            )
            when (result) {
                is com.kovcom.data.model.Result.Success -> {
                    QuotesWidgetReceiver.updateWidget(
                        context = context,
                        info = updated.toWidgetInfo()
                    )
                    updateShownQuote(updated)
                }

                is com.kovcom.data.model.Result.Error -> {
                    Timber.tag("QuotesWorker").i("showNextQuote: error = ${result.error}")
                }
            }
        }

    }

    private suspend fun showRegularQuote(quotes: List<SelectedQuoteModel>) {
        quotes.sortedBy { it.shownAt }.firstOrNull()?.let { getAndUpdateWidgetInfo(it) }
    }

    private suspend fun showNextQuote(quotes: List<SelectedQuoteModel>) {
        val currentQuote = quotes.sortedBy { it.shownAt }.lastOrNull()
        val currentQuoteIndex = quotes.indexOf(currentQuote)
        val nextQuote = when {
            currentQuoteIndex == quotes.lastIndex -> quotes.first()
            currentQuoteIndex >= 0 -> quotes[currentQuoteIndex + 1]
            else -> null
        }
        nextQuote?.let { getAndUpdateWidgetInfo(it) }
        localDataSource.setQuoteChangeOption(ExecutionOption.Regular.name)
    }

    private suspend fun showPreviousQuote(quotes: List<SelectedQuoteModel>) {
        val currentQuote = quotes.sortedBy { it.shownAt }.lastOrNull()
        val currentQuoteIndex = quotes.indexOf(currentQuote)
        val previousQuote = when {
            currentQuoteIndex == 0 -> quotes.last()
            currentQuoteIndex > 0 -> quotes[currentQuoteIndex - 1]
            else -> null
        }
        previousQuote?.let { getAndUpdateWidgetInfo(it) }
        localDataSource.setQuoteChangeOption(ExecutionOption.Regular.name)
    }

    private suspend fun updateShownQuote(quote: SelectedQuoteModel) {
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
