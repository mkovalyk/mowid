package com.kovcom.design.obj.button

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.design.theme.MoWidTheme


@Composable
fun Fab(
    icon: FabIconType,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        elevation = FloatingActionButtonDefaults.elevation(
            pressedElevation = 6.dp,
            focusedElevation = 6.dp,
        ),
    ) {
        when (icon) {
            FabIconType.Add -> {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FabPreview() {
    MoWidTheme {
        Fab(
            icon = FabIconType.Add,
            onClick = {}
        )
    }
}

enum class FabIconType {
    Add
}