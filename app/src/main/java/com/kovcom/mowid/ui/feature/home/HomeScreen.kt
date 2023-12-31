package com.kovcom.mowid.ui.feature.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.EFFECTS_KEY
import com.kovcom.mowid.base.ui.EVENTS_KEY
import com.kovcom.mowid.model.GroupPhraseUIModel
import com.kovcom.mowid.ui.composable.*
import com.kovcom.mowid.ui.composable.bottomsheet.BottomSheetScaffold
import com.kovcom.mowid.ui.composable.bottomsheet.BottomSheetScaffoldState
import com.kovcom.mowid.ui.composable.bottomsheet.rememberBottomSheetScaffoldState
import com.kovcom.mowid.ui.feature.bottomsheet.BottomSheet
import com.kovcom.mowid.ui.feature.bottomsheet.BottomSheetUIState
import com.kovcom.mowid.ui.feature.home.composable.HomeList
import com.kovcom.mowid.ui.theme.MoWidTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQuotes: (id: String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state: HomeState by viewModel.uiState.collectAsStateWithLifecycle()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current

    BackHandler(
        enabled = bottomSheetScaffoldState.bottomSheetState.isExpanded
    ) {
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
            viewModel.publishEvent(HomeEvent.HideGroupModal)
        }
    }

    LaunchedEffect(EFFECTS_KEY) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is HomeEffect.ShowError -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.collect()
    }

    LaunchedEffect(EVENTS_KEY) {
        viewModel.event.onEach { event ->
            when (event) {
                is HomeEvent.ShowGroupModal -> {
                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    } else {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }

                is HomeEvent.GroupItemClicked -> onNavigateToQuotes(event.groupPhrase.id)
                is HomeEvent.HideGroupModal -> {
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }

                is HomeEvent.AddGroupClicked -> {
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }

                is HomeEvent.OnItemDeleted -> {}
                is HomeEvent.OnEditClicked -> {
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }
            }
        }.collect()
    }


    ScreenContent(
        state = state,
        sendEvent = viewModel::publishEvent,
        bottomSheetState = bottomSheetScaffoldState,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
fun ScreenContent(
    state: HomeState,
    sendEvent: (HomeEvent) -> Unit,
    bottomSheetState: BottomSheetScaffoldState,
    onNavigateToSettings: () -> Unit,
) {

    val showMenu = remember { mutableStateOf(false) }
    val bottomSheetUIState = remember { mutableStateOf<BottomSheetUIState>(BottomSheetUIState.AddGroupBottomSheet) }

    BottomSheetScaffold(
        sheetContent = {
            BottomSheet(
                bottomSheetUIState = bottomSheetUIState.value,
                onButtonClick = { id, name, description ->
                    if (id != null) {
                        sendEvent(
                            HomeEvent.OnEditClicked(
                                id = id,
                                editedName = name,
                                editedDescription = description
                            )
                        )
                    } else {
                        sendEvent(
                            HomeEvent.AddGroupClicked(
                                name = name,
                                description = description
                            )
                        )
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
        HomeScreenContent(showMenu, onNavigateToSettings, state, bottomSheetUIState, sendEvent)
    }
}

@Composable
private fun HomeScreenContent(
    showMenu: MutableState<Boolean>,
    onNavigateToSettings: () -> Unit,
    state: HomeState,
    bottomSheetUIState: MutableState<BottomSheetUIState>,
    sendEvent: (HomeEvent) -> Unit,
) {
    Scaffold(
        topBar = { TopBar(showMenu, onNavigateToSettings) },
        floatingActionButton = {
            if (state.isLoading.not()) AppFloatingActionButton(
                onClick = {
                    bottomSheetUIState.value = BottomSheetUIState.AddGroupBottomSheet
                    sendEvent(HomeEvent.ShowGroupModal)
                }
            ) else Unit
        }
    ) { padding ->
//        Column(
//            modifier = Modifier.padding(padding)
//        ) {
        when {
            state.isLoading -> AppProgress()
            else -> HomeList(
                groupPhraseList = state.groupPhraseList,
                modifier = Modifier.padding(padding),
                onClick = {
                    sendEvent(HomeEvent.GroupItemClicked(it))
                },
                onDelete = {
                    sendEvent(HomeEvent.OnItemDeleted(it))
                },
                onEdit = { id, name, description ->
                    bottomSheetUIState.value = BottomSheetUIState.EditGroupBottomSheet(
                        id = id,
                        textField1 = name,
                        textField2 = description
                    )
                    sendEvent(HomeEvent.ShowGroupModal)
                }
            )
        }
//        }
    }
}

@Composable
private fun TopBar(showMenu: MutableState<Boolean>, onNavigateToSettings: () -> Unit) {
    AppCenterAlignedTopAppBar(
        title = stringResource(id = R.string.title_home),
        actions = {
            IconButton(onClick = { showMenu.value = !showMenu.value }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "TODO: description"
                )
            }
            AppDropDownMenu(
                expanded = showMenu.value,
                onDismissRequest = { showMenu.value = false },
                onSettingsClicked = onNavigateToSettings
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ScreenContentPreview() {
    MoWidTheme {
        val list = listOf(
            GroupPhraseUIModel(
                id = "1",
                name = "Group 0",
                description = "Description 0",
                count = 10,
                selectedCount = 5,
                canBeDeleted = true,
            ),
            GroupPhraseUIModel(
                id = "2",
                name = "Group 1",
                description = "Description 1",
                count = 10,
                selectedCount = 5,
                canBeDeleted = true,
            ),
            GroupPhraseUIModel(
                id = "3",
                name = "Group 2",
                description = "Description 2",
                count = 10,
                selectedCount = 5,
                canBeDeleted = true,
            )
        )

        ScreenContent(
            state = HomeState(
                isLoading = false,
                groupPhraseList = list
            ),
            sendEvent = {},
            bottomSheetState = rememberBottomSheetScaffoldState(),
            onNavigateToSettings = {}
        )
    }
}