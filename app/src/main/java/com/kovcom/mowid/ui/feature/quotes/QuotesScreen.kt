package com.kovcom.mowid.ui.feature.quotes

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kovcom.domain.model.GroupType
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.EVENTS_KEY
import com.kovcom.mowid.model.UiQuote
import com.kovcom.mowid.ui.composable.AppCenterAlignedTopAppBar
import com.kovcom.mowid.ui.composable.AppDropDownMenu
import com.kovcom.mowid.ui.composable.AppFloatingActionButton
import com.kovcom.mowid.ui.composable.AppProgress
import com.kovcom.mowid.ui.composable.bottomsheet.BottomSheetScaffold
import com.kovcom.mowid.ui.composable.bottomsheet.BottomSheetScaffoldState
import com.kovcom.mowid.ui.composable.bottomsheet.rememberBottomSheetScaffoldState
import com.kovcom.mowid.ui.feature.bottomsheet.BottomSheet
import com.kovcom.mowid.ui.feature.bottomsheet.BottomSheetUIState
import com.kovcom.mowid.ui.feature.quotes.QuotesContract.Intent
import com.kovcom.mowid.ui.feature.quotes.composable.EmptyState
import com.kovcom.mowid.ui.feature.quotes.composable.QuotesList
import com.kovcom.mowid.ui.theme.MoWidTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@Composable
fun QuotesScreen(
    viewModel: QuotesViewModel,
    groupName: String,
    onBackClicked: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state: QuotesContract.State by viewModel.uiState.collectAsStateWithLifecycle()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current

    BackHandler(
        enabled = bottomSheetScaffoldState.bottomSheetState.isExpanded
    ) {
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
            viewModel.processIntent(Intent.HideQuoteModal)
        }
    }

    LaunchedEffect(EVENTS_KEY) {
        viewModel.event.onEach { event ->
            when (event) {
                is QuotesContract.Event.ShowQuoteModal -> {
                    bottomSheetScaffoldState.bottomSheetState.expand()
                }

                is QuotesContract.Event.ShowError -> {
                    Timber.e(event.message)
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is QuotesContract.Event.ShowErrorRes -> {
                    Toast.makeText(context, event.resId, Toast.LENGTH_SHORT).show()
                }

                is QuotesContract.Event.ShowItemDeleted -> {

                }

                is QuotesContract.Event.ShowQuote -> {
                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    } else {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }

                QuotesContract.Event.HideQuoteModal -> {
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }
            }
        }.collect()
    }

    ScreenContent(
        groupName = groupName,
        state = state,
        sendIntent = viewModel::processIntent,
        bottomSheetState = bottomSheetScaffoldState,
        onNavigateToSettings = onNavigateToSettings,
        onBackClicked = onBackClicked,
    )

    if (state.deleteDialogInfo != null) {
        val info = state.deleteDialogInfo!!
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {
                    viewModel.processIntent(
                        Intent.DeleteQuote(
                            id = info.id,
                            isSelected = info.isSelected
                        )
                    )
                }) {
                    Text(text = stringResource(id = R.string.label_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.processIntent(Intent.HideDeleteConfirmationDialog) }) {
                    Text(text = stringResource(id = R.string.label_cancel))
                }
            },
            text = { Text(text = stringResource(id = R.string.label_delete_quote_message)) }
        )
    }
}

@Composable
fun ScreenContent(
    groupName: String,
    state: QuotesContract.State,
    sendIntent: (Intent) -> Unit,
    bottomSheetState: BottomSheetScaffoldState,
    onNavigateToSettings: () -> Unit,
    onBackClicked: () -> Unit,
) {

    var showMenu by remember { mutableStateOf(false) }
    var bottomSheetUIState: BottomSheetUIState by remember { mutableStateOf(BottomSheetUIState.AddQuoteBottomSheet) }

    BottomSheetScaffold(
        sheetContent = {
            BottomSheet(
                bottomSheetUIState = bottomSheetUIState,
                onButtonClick = { id, quote, author ->
                    if (id != null) {
                        sendIntent(
                            Intent.QuoteEditConfirmed(
                                id = id,
                                quote = quote,
                                author = author
                            )
                        )
                    } else {
                        sendIntent(Intent.AddQuoteClicked(quote, author))
                    }
                },
                clearSavedStates = bottomSheetState.bottomSheetState.isCollapsed,
                isExpanded = bottomSheetState.bottomSheetState.isExpanded,
            )
        },
        scaffoldState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        sheetPeekHeight = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Scaffold(
                topBar = {
                    AppCenterAlignedTopAppBar(
                        title = groupName,
                        actions = {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "TODO: description"
                                )
                            }
                            AppDropDownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                onSettingsClicked = onNavigateToSettings
                            )
                        },

                        navigationIcon = {
                            IconButton(onClick = onBackClicked) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (state.isLoading.not() && state.quotes.isNotEmpty()) AppFloatingActionButton(
                        onClick = {
                            bottomSheetUIState = BottomSheetUIState.AddQuoteBottomSheet
                            sendIntent(Intent.ShowQuoteModal)
                        }
                    ) else Unit
                }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding)
                ) {
                    when {
                        state.isLoading -> AppProgress()
                        state.quotes.isEmpty() -> EmptyState {
                            sendIntent(Intent.ShowQuoteModal)
                        }

                        else -> QuotesList(
                            quotes = state.quotes,
                            onCheckedChange = { quote->
                                sendIntent(
                                    Intent.QuoteChecked(
                                        quote = quote,
                                        groupType = if (quote.canBeDeleted) GroupType.Personal else GroupType.Common
                                    )
                                )
                            },
                            onItemDeleted = { id, isSelected ->
                                sendIntent(Intent.DeleteQuote(id, isSelected))
                            },
                            onEdit = { id, editedQuote, editedAuthor ->
                                bottomSheetUIState = BottomSheetUIState.EditQuoteBottomSheet(
                                    id = id,
                                    textField1 = editedQuote,
                                    textField2 = editedAuthor
                                )
                                sendIntent(Intent.ShowQuoteModal)
                            }
                        )
                    }
                }
            }
            if (bottomSheetState.bottomSheetState.isExpanded) {
                Box(
                    modifier = Modifier
                        .clickable {
                            sendIntent(Intent.HideQuoteModal)
                        }
                        .background(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20F))
                        .fillMaxSize(),
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun ScreenContentPreview() {
    MoWidTheme {
        val list = listOf(
            UiQuote(
                id = "1",
                author = "Author 1 ",
                created = "",
                quote = "Quote 1 ",
                isSelected = true,
                canBeDeleted = true,
            ),
            UiQuote(
                id = "2",
                author = "Author 2 ",
                created = "",
                quote = "Quote 2 ",
                isSelected = true,
                canBeDeleted = true,
            ),
            UiQuote(
                id = "3",
                author = "Author 3 ",
                created = "",
                quote = "Quote 3 ",
                isSelected = true,
                canBeDeleted = true,
            )
        )

        ScreenContent(
            groupName = "Group 1",
            state = QuotesContract.State(
                isLoading = false,
                quotes = list
            ),
            sendIntent = {},
            bottomSheetState = rememberBottomSheetScaffoldState(),
            onNavigateToSettings = {},
            onBackClicked = {}
        )
    }
}
