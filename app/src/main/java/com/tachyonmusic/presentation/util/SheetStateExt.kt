@file:OptIn(ExperimentalMaterialApi::class)

package com.tachyonmusic.presentation.util

import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue.Collapsed
import androidx.compose.material.BottomSheetValue.Expanded
import androidx.compose.material.ExperimentalMaterialApi

val BottomSheetState.isMoving: Boolean
    get() = isAnimationRunning || (progress.fraction > 0f && progress.fraction < 1f)

val BottomSheetState.currentFraction: Float
    get() {
        return when {
            currentValue == Collapsed && !isMovingUp -> 0f
            currentValue == Expanded && !isMovingDown -> 1f
            currentValue == Expanded && isMovingDown -> 1f - progress.fraction
            else -> progress.fraction
        }
    }

val BottomSheetState.isAtBottom: Boolean
    get() = currentFraction == 0f

val BottomSheetState.isAtTop: Boolean
    get() = currentFraction == 1f

val BottomSheetState.isMovingUp: Boolean
    get() = progress.to == Expanded

val BottomSheetState.isMovingDown: Boolean
    get() = progress.to == Collapsed