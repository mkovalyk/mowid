package com.kovcom.mowid.ui.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.kovcom.mowid.MoWidApplication
import com.kovcom.mowid.ui.worker.ExecutionOption

class LeftArrowClickAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        (context.applicationContext as MoWidApplication).workerManager.execute(ExecutionOption.Previous)
    }
}

class RightArrowClickAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        (context.applicationContext as MoWidApplication).workerManager.execute(ExecutionOption.Next)
    }
}
