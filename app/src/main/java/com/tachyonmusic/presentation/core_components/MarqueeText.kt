package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import kotlinx.coroutines.isActive


// TODO: Fade in/out gradient (https://github.com/victorbrndls/BlogProjects/blob/marquee/app/src/main/java/com/victorbrandalise/MarqueeText.kt)
//  (github implementation has a bug where text won't show at all if not moving and recomposed, thus we're using basicMarquee for now)

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.marquee(gradientEdgeColor: Color) = basicMarquee(iterations = Int.MAX_VALUE)