package com.kovcom.mowid.ui.feature.settings

import androidx.annotation.StringRes
import com.kovcom.domain.model.FrequencyModel
import com.kovcom.domain.model.User
import com.kovcom.mowid.base.ui.UserIntent
import com.kovcom.mowid.model.UiFrequency
import com.kovcom.mowid.model.UiUser

object SettingsContract {

    data class State(
        val isLoading: Boolean = false,
        val selectedFrequency: UiFrequency? = null,
        val frequencies: List<UiFrequency> = emptyList(),
        val userModel: UiUser? = null,
    ) : com.kovcom.mowid.base.ui.State

    sealed interface Intent : UserIntent {
        data class FrequencyChanged(val id: Long) : Intent
    }

    sealed interface Effect : com.kovcom.mowid.base.ui.Effect {
        data class Loading(val isLoading: Boolean) : Effect
        data class FrequenciesLoaded(val frequencies: List<FrequencyModel>) : Effect
        data class UserLoaded(val user: User?) : Effect
        data class FrequencyChanged(val id: Long) : Effect
        data class Error(val message: String) : Effect
    }

    sealed interface Event : com.kovcom.mowid.base.ui.Event {
        data class ShowToast(val message: String) : Event
        data class ShowToastId(@StringRes val message: Int) : Event
    }
}
