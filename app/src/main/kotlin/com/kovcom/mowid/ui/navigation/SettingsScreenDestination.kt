package com.kovcom.mowid.ui.navigation

import androidx.compose.runtime.Composable
import com.kovcom.mowid.ui.feature.main.MainViewModel
import com.kovcom.mowid.ui.feature.settings.SettingsScreen
import com.kovcom.mowid.ui.feature.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreenDestination(activityViewModel: MainViewModel, onBackClicked: () -> Unit) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    SettingsScreen(
        activityViewModel = activityViewModel,
        viewModel = settingsViewModel,
        onBackClicked = onBackClicked
    )
}
