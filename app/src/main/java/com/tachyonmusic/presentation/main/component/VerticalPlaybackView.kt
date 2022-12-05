package com.tachyonmusic.presentation.main.component

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
import coil.compose.rememberAsyncImagePainter
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun VerticalPlaybackView(
    playback: Playback,
    painter: Painter? = null,
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
        if (painter != null)
            Image(
                painter = painter,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .width(100.dp)
                    .height(100.dp)
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
            modifier = Modifier.padding(start = Theme.padding.small, end = Theme.padding.small),
            text = playback.title ?: "No Title",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )

        Text(
            modifier = Modifier.padding(
                start = Theme.padding.small * 2,
                bottom = Theme.padding.small,
                end = Theme.padding.small
            ),
            text = playback.artist ?: "No Artist",
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}
