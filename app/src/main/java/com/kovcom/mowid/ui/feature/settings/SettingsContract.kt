package com.kovcom.mowid.ui.feature.settings

import androidx.annotation.StringRes
import com.kovcom.mowid.base.ui.Effect
import com.kovcom.mowid.base.ui.Event
import com.kovcom.mowid.base.ui.State
import com.kovcom.mowid.model.FrequencyUIModel
import com.kovcom.mowid.model.UserUIModel

data class SettingsState(
    val isLoading: Boolean,
    val selectedFrequency: FrequencyUIModel?,
    val frequencies: List<FrequencyUIModel>,
    val userModel: UserUIModel?
) : State

sealed class SettingsEvent : Event {
    data class OnFrequencyChanged(val id: Long) : SettingsEvent()
    object BackButtonClicked : SettingsEvent()
}

sealed class SettingsEffect : Effect {
    data class ShowToast(val message: String) : SettingsEffect()
    data class ShowToastId(@StringRes val messageId: Int) : SettingsEffect()
}
