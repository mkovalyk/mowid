package com.kovcom.mowid.ui.composable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kovcom.mowid.R
import com.kovcom.mowid.ui.theme.MoWidTheme

@Composable
fun SignInAlertDialog(
    onConfirmButtonClicked: () -> Unit,
    onDismissButtonClicked: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onConfirmButtonClicked) {
                Text(text = stringResource(id = R.string.label_sign_in))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissButtonClicked) {
                Text(text = stringResource(id = R.string.label_cancel))
            }
        },
        text = { Text(text = stringResource(id = R.string.sign_in_alert_dialog_text)) }
    )
}

@Preview(showBackground = true)
@Composable
fun AppAlertDialogPreview() {
    MoWidTheme {
        SignInAlertDialog({}, {})
    }
}
