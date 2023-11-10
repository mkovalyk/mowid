package com.kovcom.mowid

import android.app.Application
import com.kovcom.data.di.dataModule
import com.kovcom.mowid.di.appModule
import com.kovcom.mowid.ui.feature.quotes.quotesModule
import com.kovcom.mowid.ui.feature.settings.settingsModule
import com.kovcom.mowid.ui.worker.ExecutionOption
import com.kovcom.mowid.ui.worker.QuotesWorkerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class MoWidApplication : Application() {

    val workerManager: QuotesWorkerManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MoWidApplication)
            workManagerFactory()
            modules(
                dataModule,
                appModule,
                settingsModule,
                quotesModule,
            )
        }

        CoroutineScope(Dispatchers.IO)
            .launch {
                workerManager.execute(ExecutionOption.Regular)
            }
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
