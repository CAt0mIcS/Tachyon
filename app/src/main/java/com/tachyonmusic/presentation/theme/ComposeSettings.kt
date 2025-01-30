package com.tachyonmusic.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms

data class ComposeSettings(
    val animateText: Boolean = true,
    val dynamicColors: Boolean = true,
    val audioUpdateInterval: Duration = 100.ms,
    val startDestination: String = ""
)

val LocalSettings = compositionLocalOf { ComposeSettings() }