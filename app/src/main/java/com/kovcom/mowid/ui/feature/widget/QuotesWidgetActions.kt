package com.kovcom.mowid.ui.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import org.koin.java.KoinJavaComponent.inject

class LeftArrowClickAction : ActionCallback {

//    val workManager: QuotesWorkerManager by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
//        workManager.execute(ExecutionOption.PREVIOUS)
    }
}

class RightArrowClickAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
//        val workManager =
//            EntryPointAccessors.fromApplication<ActionCallBackEntryPoint>(context).workManager()
//        workManager.execute(ExecutionOption.NEXT)
    }
}
