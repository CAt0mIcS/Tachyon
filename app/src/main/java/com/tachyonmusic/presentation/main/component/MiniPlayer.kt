package com.tachyonmusic.presentation.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
fun MiniPlayer(
    playback: Playback,
    currentPosition: Float,
    artwork: ImageBitmap? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Theme.padding.extraSmall,
                top = Theme.padding.extraSmall,
                end = Theme.padding.extraSmall
            )
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(Theme.colors.tertiary, shape = Theme.shapes.medium)
    ) {
        if (artwork != null)
            Image(
                bitmap = artwork,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(48.dp, 48.dp)
                    .clip(Theme.shapes.medium)
            )
        else
            Image(
                painterResource(R.drawable.artwork_image_placeholder),
                "Album Artwork Placeholder",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(48.dp, 48.dp)
                    .clip(Theme.shapes.medium)
            )

        Row(
            modifier = Modifier.padding(start = Theme.padding.small),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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

            IconButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_play), contentDescription = "Play")
            }
        }
    }

    ProgressIndicator(
        progress = currentPosition,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Theme.padding.medium, end = Theme.padding.medium),
        color = Theme.colors.orange,
        backgroundColor = Theme.colors.partialOrange
    )
}