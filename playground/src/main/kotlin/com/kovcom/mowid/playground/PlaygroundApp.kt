package com.kovcom.mowid.playground

import android.app.Application
import timber.log.Timber

class PlaygroundApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}