package com.tachyonmusic.presentation.theme

import androidx.compose.runtime.compositionLocalOf

data class Animation(
    val none: Int = 0,
    val short: Int = 200,
    val medium: Int = 500,
    val long: Int = 1000,
    val default: Int = none
)

val LocalAnimation = compositionLocalOf { Animation() }