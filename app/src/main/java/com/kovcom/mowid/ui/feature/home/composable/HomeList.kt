package com.kovcom.mowid.ui.feature.home.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.domain.model.GroupType
import com.kovcom.mowid.Label
import com.kovcom.mowid.model.UiGroup
import com.kovcom.mowid.ui.feature.home.HomeState
import com.kovcom.mowid.ui.feature.home.HomeUserIntent
import com.kovcom.mowid.ui.feature.quotes.composable.SwipeToDeleteBackground
import com.kovcom.mowid.ui.theme.MoWidTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeList(
    groupPhraseList: List<UiGroup>,
    dialogType: HomeState.DialogType,
    onClick: (groupPhrase: UiGroup) -> Unit,
    onDelete: (UiGroup) -> Unit,
    onEdit: (id: String, name: String, description: String) -> Unit,
    sendIntent: (intent: HomeUserIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            items(items = groupPhraseList) { item ->
                val currentItem by rememberUpdatedState(item)
                val dismissState = rememberDismissState(
                    confirmValueChange = {
                        if (it == DismissValue.DismissedToStart) {
                            onDelete(currentItem)
                        }
                        true
                    },
                    positionalThreshold = { 200.dp.toPx() }
                )
                LaunchedEffect(item) {
                    if (!item.isSwipeToDeleteOpened) {
                        dismissState.reset()
                    }
                }

                if (item.canBeDeleted) {
                    SwipeToDismiss(
                        modifier = Modifier.animateItemPlacement(),
                        state = dismissState,
                        background = { SwipeToDeleteBackground() },
                        dismissContent = {
                            HomeListItem(
                                groupPhrase = item,
                                onClick = onClick,
                                onEdit = onEdit
                            )
                        },
                        directions = setOf(DismissDirection.EndToStart),
                    )
                } else {
                    HomeListItem(
                        groupPhrase = item,
                        onClick = onClick,
                        onEdit = onEdit
                    )
                }
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

        }
        when (dialogType) {
            is HomeState.DialogType.RemoveGroupConfirmation -> {
                AlertDialog(onDismissRequest = {}, confirmButton = {
                    TextButton(onClick = {
                        sendIntent(
                            HomeUserIntent.RemoveGroupConfirmed(
                                id = dialogType.id,
                                name = dialogType.name,
                                groupType = dialogType.groupType,
                            )
                        )
                    }) {
                        Text(text = Label.Delete.value)
                    }
                }, dismissButton = {
                    TextButton(onClick = { sendIntent(HomeUserIntent.HideGroupConfirmationDialog) }) {
                        Text(text = Label.Cancel.value)
                    }
                }, text = { Text(text = "TO be changed") } // TODO change
                )
            }

            is HomeState.DialogType.LoginToProceed -> {

            }

            is HomeState.DialogType.None -> Unit
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeListPreview() {
    MoWidTheme {
        HomeList(
            listOf(
                UiGroup(
                    id = "1",
                    name = "Group 0",
                    description = "Description 0",
                    count = 10,
                    selectedCount = 5,
                    groupType = GroupType.Common,
                ),
                UiGroup(
                    id = "2",
                    name = "Group 1",
                    description = "Description 1",
                    count = 10,
                    selectedCount = 5,
                    groupType = GroupType.Common,
                ),
                UiGroup(
                    id = "3",
                    name = "Group 2",
                    description = "Description 2",
                    count = 10,
                    selectedCount = 5,
                    groupType = GroupType.Common,
                )
            ),
            dialogType = HomeState.DialogType.None,
            onClick = {},
            onDelete = {},
            onEdit = { _, _, _ -> },
            sendIntent = {},
        )
    }
}
