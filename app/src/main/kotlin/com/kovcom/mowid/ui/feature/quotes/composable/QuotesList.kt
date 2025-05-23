package com.kovcom.mowid.ui.feature.quotes.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.mowid.model.UiQuote

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuotesList(
    quotes: List<UiQuote>,
    onCheckedChange: (UiQuote) -> Unit,
    onItemDeleted: (String, Boolean) -> Unit,
    onEdit: (id: String, quote: String, author: String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        items(
            items = quotes,
            key = { quoteModel -> quoteModel.id }
        ) { currentItem ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.StartToEnd) {
                        onItemDeleted(currentItem.id, currentItem.isSelected)
                    }
                    true
                },
                positionalThreshold = { 200.dp.value }
            )

            LaunchedEffect(currentItem) {
                if (!currentItem.isExpanded) {
                    dismissState.reset()
                }
            }

            if (currentItem.canBeDeleted) {
                SwipeToDismissBox(
                    modifier = Modifier.animateItemPlacement(),
                    state = dismissState,
                    backgroundContent = { SwipeToDeleteBackground() },
                    content = {
                        QuoteListItem(
                            quote = currentItem,
                            onCheckChanged = onCheckedChange,
                            onEdit = onEdit
                        )
                    },
                    enableDismissFromEndToStart = true,
//                    directions = setOf(DismissDirection.EndToStart),
                )
            } else {
                QuoteListItem(
                    quote = currentItem,
                    onCheckChanged = onCheckedChange,
                    onEdit = onEdit
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuotesListPreview() {
    com.kovcom.design.theme.MoWidTheme {
        QuotesList(
            listOf(
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
            ),
            onCheckedChange = { _ -> },
            onItemDeleted = { _, _ -> },
            onEdit = { _, _, _ -> }
        )
    }
}
