package com.kovcom.design.obj

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.design.theme.MoWidTheme

@Composable
fun SecondaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
        )
    ) {
        Text(
            text = text,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecondaryButtonPreview() {
    MoWidTheme {
        Column {
            SecondaryButton(text = "Secondary Button") {}
            SecondaryButton(text = "Secondary Button", enabled = false) {}
        }
    }
}