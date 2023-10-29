package com.kovcom.mowid.ui.feature.home.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kovcom.mowid.model.UiGroup
import com.kovcom.mowid.ui.theme.MoWidTheme

@Composable
fun HomeListItem(
    groupPhrase: UiGroup,
    onClick: (groupPhrase: UiGroup) -> Unit,
    onEdit: (id: String, name: String, description: String) -> Unit,
) {

    Row(
        modifier = Modifier
            .height(88.dp)
            .background(MaterialTheme.colorScheme.onPrimary)
            .pointerInput(groupPhrase) {
                detectTapGestures(
                    onLongPress = {
                        if (groupPhrase.canBeDeleted) {
                            onEdit(groupPhrase.id, groupPhrase.name, groupPhrase.description)
                        }
                    },
                    onTap = {
                        onClick(groupPhrase)
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .padding(start = 16.dp)
        ) {
            Text(
                text = groupPhrase.name,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = groupPhrase.description,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .padding(end = 16.dp)
        )
        {
            Text(
                text = "${groupPhrase.selectedCount}/${groupPhrase.count}",
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeListItemPreview() {
    MoWidTheme {
        HomeListItem(
            UiGroup(
                id = "1",
                name = "Group 0",
                description = "Description 0",
                count = 10,
                selectedCount = 5,
                canBeDeleted = true,
            ),
            onClick = {},
            onEdit = { _, _, _ -> }
        )
    }
}