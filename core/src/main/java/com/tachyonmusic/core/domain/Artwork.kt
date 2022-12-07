package com.tachyonmusic.core.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

interface Artwork {
    @Composable
    fun Image(contentDescription: String?, modifier: Modifier)
}