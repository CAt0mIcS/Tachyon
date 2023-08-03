package com.tachyonmusic.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tachyonmusic.util.UiText

@Composable
fun UiText.asString() = when(this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> stringResource(resId, *arguments)
}