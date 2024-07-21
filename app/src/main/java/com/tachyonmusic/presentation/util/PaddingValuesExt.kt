package com.tachyonmusic.presentation.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDir = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDir) + other.calculateStartPadding(layoutDir),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(layoutDir) + other.calculateEndPadding(layoutDir),
        bottom = calculateBottomPadding() + other.calculateBottomPadding()
    )
}

@Composable
fun PaddingValues.copy(
    start: Dp? = null,
    end: Dp? = null,
    top: Dp? = null,
    bottom: Dp? = null
): PaddingValues {
    val layoutDir = LocalLayoutDirection.current
    return PaddingValues(
        start = start ?: calculateStartPadding(layoutDir),
        top = top ?: calculateTopPadding(),
        end = end ?: calculateEndPadding(layoutDir),
        bottom = bottom ?: calculateBottomPadding()
    )
}