package com.kovcom.mowid.ui.feature.quotes.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.mowid.ui.theme.MoWidTheme

@Composable
fun SwipeToDeleteBackground() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 16.dp),
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete quote"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeToDeleteBackgroundPreview() {
    MoWidTheme {
        SwipeToDeleteBackground()
    }
}
