package com.tachyonmusic.presentation.util

import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi

@OptIn(ExperimentalMaterialApi::class)
val BottomSheetState.isMoving: Boolean
    get() = isAnimationRunning || (progress.fraction > 0f && progress.fraction < 1f)