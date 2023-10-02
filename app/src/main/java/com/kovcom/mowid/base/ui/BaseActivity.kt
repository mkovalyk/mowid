package com.kovcom.mowid.base.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

abstract class BaseActivity<
    State : UiState,
    Event : UiEvent,
    Effect : UiEffect,
    ViewModel : BaseViewModel<State, Event, Effect>> :
    ComponentActivity() {

    protected abstract val viewModel: ViewModel

    protected abstract fun handleEffect(effect: Effect)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeOnEffect()
    }

    private fun subscribeOnEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect {
                    handleEffect(it)
                }
            }
        }
    }
}