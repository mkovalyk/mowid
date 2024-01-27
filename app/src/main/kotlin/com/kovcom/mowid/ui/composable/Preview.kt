package com.kovcom.mowid.ui.composable

import androidx.compose.runtime.Composable
import org.dynodict.DynoDict
import org.dynodict.android.initWithEmpty

@Composable
fun PreviewComposable(content: @Composable () -> Unit) {
    DynoDict.initWithEmpty()
    content()
}