package com.kovcom.mowid.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppFloatingActionButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = {
            onClick()
        },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Icon(Icons.Filled.Add, "TODO: description")
    }
}

@Preview(showBackground = true)
@Composable
fun AppFloatingActionButtonPreview() {
    com.kovcom.design.theme.MoWidTheme {
        AppFloatingActionButton(
            onClick = {}
        )
    }
}