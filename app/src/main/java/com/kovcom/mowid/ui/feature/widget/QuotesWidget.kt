package com.kovcom.mowid.ui.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.*
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.kovcom.mowid.R
import com.kovcom.mowid.ui.feature.widget.composable.QuotesWidgetContent
import timber.log.Timber

class QuotesWidget : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Timber.tag(TAG).i("provideGlance: $id")
        provideContent {
            Content()
        }
    }

    companion object {

        private const val QUOTE_PREFS_KEY = "QUOTE_PREFS_KEY"
        private const val AUTHOR_PREFS_KEY = "AUTHOR_PREFS_KEY"
        private const val QUOTE_ID_PREFS_KEY = "QUOTE_ID_PREFS_KEY"
        private const val GROUP_ID_PREFS_KEY = "GROUP_ID_PREFS_KEY"
        private const val TAG = "QuotesWidget"

        val quotePreference = stringPreferencesKey(QUOTE_PREFS_KEY)
        val authorPreference = stringPreferencesKey(AUTHOR_PREFS_KEY)
        val quoteIdPreference = stringPreferencesKey(QUOTE_ID_PREFS_KEY)
        val groupIdPreference = stringPreferencesKey(GROUP_ID_PREFS_KEY)
    }
}

class QuotesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = QuotesWidget()

    companion object {

        suspend fun updateWidget(context: Context, info: WidgetQuoteInfo) {
            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(QuotesWidget::class.java).lastOrNull()

            if (glanceId == null) {
                Timber.tag("QuotesWidgetReceiver").e("updateWidget: glanceId is null")
                return
            }

            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[QuotesWidget.groupIdPreference] = info.groupId
                prefs[QuotesWidget.quoteIdPreference] = info.quoteId
                prefs[QuotesWidget.quotePreference] = info.quote
                prefs[QuotesWidget.authorPreference] = info.author
            }
            QuotesWidget().updateAll(context)
        }
    }
}

data class WidgetQuoteInfo(
    val quote: String,
    val author: String,
    val quoteId: String,
    val groupId: String,
)

@Composable
private fun Content() {
    QuotesWidgetContent(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(androidx.glance.ImageProvider(R.drawable.widget_background))
            .appWidgetBackground()
            .padding(8.dp),
    )
}
