package com.tachyonmusic.presentation.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection

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