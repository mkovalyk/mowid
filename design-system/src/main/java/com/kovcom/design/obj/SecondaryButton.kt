package com.kovcom.design.obj

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.design.theme.MoWidTheme
import com.kovcom.design.theme.TextColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = MaterialTheme.colorScheme.secondaryContainer,
            rippleAlpha = RippleAlpha(1f, 1f, 1f, 0.8f),
        )
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
                style = MaterialTheme.typography.labelMedium,
                color = TextColors.onSecondary
            )
        }
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