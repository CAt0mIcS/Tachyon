package com.tachyonmusic.presentation.main.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun PlaybackView(playback: Playback, artwork: ImageBitmap? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(Theme.colors.secondary, shape = Theme.shapes.medium)
            .border(BorderStroke(1.dp, Theme.colors.border), shape = Theme.shapes.medium)
    ) {
        if (artwork != null)
            Image(
                bitmap = artwork,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(100.dp, 100.dp)
                    .clip(Theme.shapes.medium)
            )
        else
            Image(
                painterResource(R.drawable.artwork_image_placeholder),
                "Album Artwork Placeholder",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(100.dp, 100.dp)
                    .clip(Theme.shapes.medium)
            )

        Text(
            modifier = Modifier.padding(start = Theme.padding.small),
            text = playback.title ?: "No Title",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Text(
            modifier = Modifier.padding(
                start = Theme.padding.small * 2,
                bottom = Theme.padding.small
            ),
            text = playback.artist ?: "No Artist",
            fontSize = 12.sp
        )
    }
}