package com.tachyonmusic.presentation.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.isEnabled(enabled: Boolean, disabledAlpha: Float = .6f) =
    graphicsLayer { alpha = if (enabled) 1f else disabledAlpha }