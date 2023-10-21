package com.kovcom.mowid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kovcom.mowid.ui.feature.main.MainViewModel
import com.kovcom.mowid.ui.feature.settings.SettingsScreen
import com.kovcom.mowid.ui.feature.settings.SettingsViewModel

@Composable
fun SettingsScreenDestination(activityViewModel: MainViewModel, onBackClicked: () -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel()
    SettingsScreen(
        activityViewModel = activityViewModel,
        viewModel = settingsViewModel,
        onBackClicked = onBackClicked
    )
}
