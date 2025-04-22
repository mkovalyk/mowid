package com.kovcom.design.obj.button

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = MaterialTheme.colorScheme.primaryContainer,
            rippleAlpha = RippleAlpha(1f, 1f, 1f, 1f),
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryButtonPreview() {
    MoWidTheme {
        Column {
            PrimaryButton(text = "Primary Button") {}
            PrimaryButton(text = "Primary Button", enabled = false) {}
        }
    }
}