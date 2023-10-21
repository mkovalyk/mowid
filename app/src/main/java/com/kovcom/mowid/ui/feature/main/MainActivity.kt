package com.kovcom.mowid.ui.feature.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.BaseActivity
import com.kovcom.mowid.ui.navigation.AppNavigation
import com.kovcom.mowid.ui.theme.MoWidTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : BaseActivity<MainState, MainEvent, MainEffect, MainViewModel>() {

    override val viewModel: MainViewModel by viewModel()

    private val groupId: String?
        get() = intent?.getStringExtra(GROUP_ID)

    private val quoteId: String?
        get() = intent?.getStringExtra(QUOTE_ID)

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.viewModel.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeOnEvent()
        setContent {
            MoWidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }

            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val groupId = groupId
                        val quoteId = quoteId
                        if (groupId != null && quoteId != null) {
                            viewModel.navigateToQuote(groupId, quoteId)
                        }
                    }
                }
            }
        }
    }

    override fun handleEffect(effect: MainEffect) {
        when (effect) {
            is MainEffect.ShowToast -> {
                Timber.i("effect.messageId: ${effect.messageId}")
                Toast.makeText(this, getString(effect.messageId), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun subscribeOnEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.event.collect {
                    when (it) {
                        is MainEvent.SignIn -> createSignInIntent()
                        is MainEvent.SignOut -> signOut()
                        is MainEvent.NavigateToQuote -> {
                            // do nothing. It is handled in AppNavigation
                        }
                    }
                }
            }
        }
    }

    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.LoginTheme)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                viewModel.signOutSuccess()
                Toast.makeText(this, getString(R.string.label_sign_out_success), Toast.LENGTH_LONG).show()
            }
    }

    companion object {

        private const val TAG = "MainActivity"
        private const val GROUP_ID = "groupId"
        private const val QUOTE_ID = "quoteId"

        fun start(context: Context, groupId: String, quoteId: String) {
            val intent = Intent(context, MainActivity::class.java)
                .apply {
                    putExtra(GROUP_ID, groupId)
                    putExtra(QUOTE_ID, quoteId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            context.startActivity(intent)
        }
    }
}
