package com.tachyonmusic.presentation.core_components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun HorizontalPlaybackView(
    playback: Playback,
    artwork: Artwork,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(Theme.shadow.extraSmall, shape = Theme.shapes.medium)
            .background(Theme.colors.secondary, shape = Theme.shapes.medium)
            .border(BorderStroke(1.dp, Theme.colors.border), shape = Theme.shapes.medium)
    ) {
        artwork.Image(
            contentDescription = "Album Artwork",
            modifier = Modifier
                .padding(Theme.padding.extraSmall)
                .size(50.dp, 50.dp)
                .clip(Theme.shapes.medium)
        )

        Column(modifier = Modifier.padding(start = Theme.padding.small)) {
            Text(
                modifier = Modifier.padding(top = Theme.padding.small),
                text = playback.title ?: "No Title",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2
            )

            Text(
                modifier = Modifier.padding(
                    start = Theme.padding.small,
                    bottom = Theme.padding.small
                ),
                text = playback.artist ?: "No Artist",
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
