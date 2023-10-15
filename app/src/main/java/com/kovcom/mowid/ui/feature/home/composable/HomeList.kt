package com.kovcom.mowid.ui.feature.home.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.mowid.model.GroupPhraseUIModel
import com.kovcom.mowid.ui.feature.quotes.composable.SwipeToDeleteBackground
import com.kovcom.mowid.ui.theme.MoWidTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeList(
    groupPhraseList: List<GroupPhraseUIModel>,
    onClick: (groupPhrase: GroupPhraseUIModel) -> Unit,
    onDelete: (id: String, name: String) -> Unit,
    onEdit: (id: String, name: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        items(items = groupPhraseList) { item ->
            val currentItem by rememberUpdatedState(item)
            val dismissState = rememberDismissState(
                confirmValueChange = {
                    if (it == DismissValue.DismissedToStart) {
                        onDelete(currentItem.id,currentItem.name)
                    }
                    true
                },
                positionalThreshold = { 200.dp.toPx() }
            )
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
}

@Preview(showBackground = true)
@Composable
fun HomeListPreview() {
    MoWidTheme {
        HomeList(
            listOf(
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
            ),
            onClick = {},
            onDelete = {_,_ -> },
            onEdit = { _, _, _ -> }
        )
    }
}
