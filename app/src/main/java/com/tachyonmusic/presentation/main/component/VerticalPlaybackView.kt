package com.tachyonmusic.presentation.main.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.presentation.core_components.MarqueeText
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.displaySubtitle
import com.tachyonmusic.presentation.util.displayTitle

@Composable
fun VerticalPlaybackView(
    playback: SinglePlayback,
    artwork: Artwork,
    isArtworkLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            // Constrain width to padding + album art size (TODO)
            // TODO: text should be animated to move in if it's too long
            .width(Theme.padding.extraSmall * 2 + 100.dp)
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(Theme.colors.secondary, shape = Theme.shapes.medium)
            .border(BorderStroke(1.dp, Theme.colors.border), shape = Theme.shapes.medium)
    ) {
        if (isArtworkLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .width(100.dp)
                    .height(100.dp)
                    .clip(Theme.shapes.medium)
            )
        } else
            artwork.Image(
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(100.dp, 100.dp)
                    .clip(Theme.shapes.medium)
            )

        MarqueeText(
            modifier = Modifier.padding(start = Theme.padding.small, end = Theme.padding.small),
            text = playback.displayTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            gradientEdgeColor = Theme.colors.secondary
        )

        MarqueeText(
            modifier = Modifier.padding(
                start = Theme.padding.small * 2,
                bottom = Theme.padding.small,
                end = Theme.padding.small
            ),
            text = playback.displaySubtitle,
            fontSize = 10.sp,
            gradientEdgeColor = Theme.colors.secondary
        )
    }
}
