package com.kovcom.mowid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kovcom.mowid.ui.feature.home.HomeScreen
import com.kovcom.mowid.ui.feature.home.HomeViewModel
import com.kovcom.mowid.ui.feature.main.MainEvent

@Composable
fun HomeScreenDestination(
    sendMainEvent: (MainEvent) -> Unit,
    onNavigateToQuotes: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel()
    HomeScreen(
        viewModel = viewModel,
        sendMainEvent = sendMainEvent,
        onNavigateToQuotes = onNavigateToQuotes,
        onNavigateToSettings = onNavigateToSettings
    )
}