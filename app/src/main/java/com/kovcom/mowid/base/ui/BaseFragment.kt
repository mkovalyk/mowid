package com.kovcom.mowid.base.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

abstract class BaseFragment<
    State : UiState,
    Event : UiEvent,
    Effect : UiEffect,
    ViewModel : BaseViewModel<State, Event, Effect>> :
    Fragment() {

    protected abstract val viewModel: ViewModel

    protected abstract fun handleEffect(effect: Effect)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeOnEffect()
    }

    private fun subscribeOnEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.effect.collect {
                    handleEffect(it)
                }
            }
        }
    }
}