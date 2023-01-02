package com.tachyonmusic.presentation.util

import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeProgress
import androidx.compose.material.SwipeableState

@OptIn(ExperimentalMaterialApi::class)
val BottomSheetState.isMoving: Boolean
    get() = isAnimationRunning || (progress.fraction > 0f && progress.fraction < 1f)

@OptIn(ExperimentalMaterialApi::class)
val BottomSheetState.currentFraction: Float
    get() {
        return when {
            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Collapsed -> 0f
            currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.Expanded -> 1f
            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Expanded -> progress.fraction
            else -> 1f - progress.fraction
        }
    }