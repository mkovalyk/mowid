package com.kovcom.mowid.ui.navigation

import androidx.compose.runtime.Composable
import com.kovcom.mowid.ui.feature.quotes.QuotesScreen
import com.kovcom.mowid.ui.feature.quotes.QuotesViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun QuotesScreenDestination(
    groupId: String,
    onBackClicked: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val viewModel: QuotesViewModel = koinViewModel { parametersOf(groupId) }
    QuotesScreen(viewModel, onBackClicked, onNavigateToSettings)
}
