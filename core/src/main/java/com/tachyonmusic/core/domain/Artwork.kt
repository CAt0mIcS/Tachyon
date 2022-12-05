package com.tachyonmusic.core.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

interface Artwork {
    val painter: Painter
        @Composable get
}