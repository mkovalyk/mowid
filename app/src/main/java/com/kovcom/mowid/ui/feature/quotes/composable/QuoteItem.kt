package com.kovcom.mowid.ui.feature.quotes.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kovcom.mowid.model.UiQuote
import com.kovcom.mowid.ui.theme.MoWidTheme

@Composable
fun QuoteListItem(
    quote: UiQuote,
    onCheckChanged: (String, Boolean) -> Unit,
    onEdit: (id: String, quote: String, author: String) -> Unit,
) {
    var checkedState by rememberSaveable { mutableStateOf(quote.isSelected) }

    QuoteListItem(
        quote = quote,
        checked = checkedState,
        onCheckChanged = { id, checked ->
            checkedState = checked
            onCheckChanged(id, checked)
        },
        onEdit = onEdit
    )
}

@Composable
fun QuoteListItem(
    quote: UiQuote,
    checked: Boolean,
    onCheckChanged: (String, Boolean) -> Unit,
    onEdit: (id: String, quote: String, author: String) -> Unit,
) {

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.onPrimary)
            .clickable(enabled = !quote.canBeDeleted, onClick = { onCheckChanged(quote.id, !checked) })
            .pointerInput(quote) {
                if (quote.canBeDeleted) {
                    detectTapGestures(
                        onTap = {
                            onEdit(quote.id, quote.quote, quote.author)
                        }
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .padding(start = 16.dp)
        )
        {
            Text(
                text = quote.quote,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                modifier = Modifier.align(Alignment.End),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = quote.author,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Checkbox(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            checked = checked,
            onCheckedChange = { isChecked ->
                onCheckChanged(quote.id, isChecked)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteListItemPreview() {
    MoWidTheme {
        QuoteListItem(
            UiQuote(
                id = "1",
                author = "Author",
                created = "",
                quote = "Quote",
                isSelected = true,
                canBeDeleted = true
            ),
            checked = true,
            onCheckChanged = { _, _ -> },
            onEdit = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteListItemLongPreview() {
    MoWidTheme {
        QuoteListItem(
            UiQuote(
                id = "1",
                author = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore ",
                created = "",
                quote = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea",
                isSelected = false,
                canBeDeleted = true
            ),
            checked = true,
            onCheckChanged = { _, _ -> },
            onEdit = { _, _, _ -> }
        )
    }
}