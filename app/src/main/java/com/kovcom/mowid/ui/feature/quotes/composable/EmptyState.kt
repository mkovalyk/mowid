package com.kovcom.mowid.ui.feature.quotes.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.mowid.R
import com.kovcom.mowid.ui.theme.MoWidTheme

@Composable
fun EmptyState(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_state),
            contentDescription = "empty state",
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 42.dp)
                .padding(top = 180.dp),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.label_empty_state)
        )
        Button(
            modifier = Modifier.padding(top = 44.dp),
            onClick = onClick
        )
        {
            Text(
                text = stringResource(id = R.string.label_add),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatPreview() {
    MoWidTheme {
        EmptyState {}
    }
}
