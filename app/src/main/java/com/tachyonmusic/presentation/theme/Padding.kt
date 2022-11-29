package com.tachyonmusic.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Padding(
    val none: Dp = 0.dp,
    val extremelySmall: Dp = 3.dp,
    val extraSmall: Dp = 6.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 32.dp,
    val default: Dp = none
)

val LocalPadding = compositionLocalOf { Padding() }