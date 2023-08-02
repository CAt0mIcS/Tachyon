package com.tachyonmusic.presentation.core_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun LoadingBox(
    zIndex: Float = 100f,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(Theme.colors.primary)
        .zIndex(zIndex)
) {
    Box(
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = Theme.colors.contrastHigh
        )
    }
}