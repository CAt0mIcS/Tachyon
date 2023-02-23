package com.tachyonmusic.presentation.theme

import androidx.compose.runtime.compositionLocalOf

data class ComposeSettings(
    val animateText: Boolean = true
)

val LocalSettings = compositionLocalOf { ComposeSettings() }