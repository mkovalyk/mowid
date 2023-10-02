package com.kovcom.mowid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.kovcom.mowid.ui.feature.quotes.QuotesScreen
import com.kovcom.mowid.ui.feature.quotes.QuotesViewModel

@Composable
fun QuotesScreenDestination(
    groupName: String,
    onBackClicked: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: QuotesViewModel = hiltViewModel()
    QuotesScreen(viewModel, groupName, onBackClicked, onNavigateToSettings)
}
