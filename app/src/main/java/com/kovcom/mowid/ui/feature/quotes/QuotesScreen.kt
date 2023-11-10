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
import com.kovcom.mowid.ui.feature.quotes.composable.EmptyState
import com.kovcom.mowid.ui.feature.quotes.composable.QuotesList
import com.kovcom.mowid.ui.theme.MoWidTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@Composable
fun QuotesScreen(
    viewModel: QuotesViewModel2,
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
            viewModel.processIntent(QuotesContract.Intent.HideQuoteModal)
        }
    }

//    LaunchedEffect(EFFECTS_KEY) {
//        viewModel.effect.onEach { effect ->
//            when (effect) {
//
//                is QuotesEffect.ShowError -> {
//                    Timber.e("${effect.message}")
//                    Toast.makeText(
//                        context,
//                        effect.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }.collect()
//    }

    LaunchedEffect(EVENTS_KEY) {
        viewModel.event.onEach { event ->
            when (event) {
//                is QuotesEvent.ShowQuoteModal -> {
//                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
//                        bottomSheetScaffoldState.bottomSheetState.expand()
//                    } else {
//                        bottomSheetScaffoldState.bottomSheetState.collapse()
//                    }
//                }

//                is QuotesEvent.QuoteItemChecked -> {}
//                is QuotesEvent.HideQuoteModal -> {
//                    bottomSheetScaffoldState.bottomSheetState.collapse()
//                }
//
//                is QuotesEvent.AddQuoteClicked -> {
//                    bottomSheetScaffoldState.bottomSheetState.collapse()
//                }
//
//                is QuotesEvent.BackButtonClicked -> onBackClicked()
//                is QuotesEvent.OnItemDeleted -> {}
//                is QuotesEvent.OnEditClicked -> {
//                    bottomSheetScaffoldState.bottomSheetState.collapse()
//                }

//                is QuotesEvent.ShowDeleteConfirmationDialog -> {}
//                is QuotesEvent.HideDeleteConfirmationDialog -> {}
                QuotesContract.Event.ShowAddQuoteModal -> {
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
            }
        }.collect()
    }

    ScreenContent(
        groupName = groupName,
        state = state,
        sendEvent = viewModel::processIntent,
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
                        QuotesContract.Intent.DeleteQuote(
                            id = info.id,
                            isSelected = info.isSelected
                        )
                    )
                }) {
                    Text(text = stringResource(id = R.string.label_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.processIntent(QuotesContract.Intent.HideDeleteConfirmationDialog) }) {
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
    sendEvent: (QuotesContract.Intent) -> Unit,
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
                        sendEvent(
                            QuotesContract.Intent.QuoteEditConfirmed(
                                id = id,
                                quote = quote,
                                author = author
                            )
                        )
                    } else {
                        sendEvent(QuotesContract.Intent.AddQuoteClicked(quote, author))
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
                            sendEvent(QuotesContract.Intent.ShowQuoteModal)
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
                            sendEvent(QuotesContract.Intent.ShowQuoteModal)
                        }

                        else -> QuotesList(
                            quotes = state.quotes,
                            onCheckedChange = { id, checked ->
                                val quote = state.quotes.firstOrNull { it.id == id }
                                sendEvent(
                                    QuotesContract.Intent.QuoteItemChecked(
                                        quoteId = id,
                                        quote = quote?.quote ?: "",
                                        author = quote?.author,
                                        checked = checked,
                                        groupType = if (quote?.canBeDeleted == true) GroupType.Personal else GroupType.Common
                                    )
                                )
                            },
                            onItemDeleted = { id, isSelected ->
                                sendEvent(QuotesContract.Intent.ShowDeleteConfirmationDialog(id, isSelected))
                            },
                            onEdit = { id, editedQuote, editedAuthor ->
                                bottomSheetUIState = BottomSheetUIState.EditQuoteBottomSheet(
                                    id = id,
                                    textField1 = editedQuote,
                                    textField2 = editedAuthor
                                )
                                sendEvent(QuotesContract.Intent.ShowQuoteModal)
                            }
                        )
                    }
                }
            }
            if (bottomSheetState.bottomSheetState.isExpanded) {
                Box(
                    modifier = Modifier
                        .clickable {
                            sendEvent(QuotesContract.Intent.HideQuoteModal)
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
            sendEvent = {},
            bottomSheetState = rememberBottomSheetScaffoldState(),
            onNavigateToSettings = {},
            onBackClicked = {}
        )
    }
}
