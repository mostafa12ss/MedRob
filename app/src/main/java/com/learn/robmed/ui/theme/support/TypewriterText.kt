package com.learn.robmed.ui.theme.support

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay


@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    delayMs: Long = 60L,
    startDelayMs: Long = 0L,
    onFinished: () -> Unit = {},
) {
    var shown by remember(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        shown = ""
        delay(startDelayMs)
        for (i in 1..text.length) {
            shown = text.subSequence(0, i).toString()
            delay(delayMs)
        }
        onFinished()
    }

    Text(text = shown, modifier = modifier, style = style)
}

