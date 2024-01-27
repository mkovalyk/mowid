package com.kovcom.mowid.ui.composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kovcom.mowid.Title

@Composable
fun AppDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text(Title.Settings.value) },
            onClick = {
                onSettingsClicked()
                onDismissRequest()
            })
    }
}
