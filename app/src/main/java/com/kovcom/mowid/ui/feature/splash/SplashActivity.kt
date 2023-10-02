package com.kovcom.mowid.ui.feature.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kovcom.mowid.ui.feature.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private val groupId = intent?.getStringExtra(GROUP_ID)
    private val quoteId = intent?.getStringExtra(QUOTE_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { true }
        }
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                delay(1000)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    companion object {

        private const val GROUP_ID = "groupId"
        private const val QUOTE_ID = "quoteId"

        fun start(context: Context, groupId: String?, quoteId: String?) {
            val intent = Intent(context, MainActivity::class.java)
                .apply {
                    putExtra(GROUP_ID, groupId)
                    putExtra(QUOTE_ID, quoteId)
                }
            context.startActivity(intent)
        }
    }
}
