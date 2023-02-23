package com.tachyonmusic.presentation.core_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.displaySubtitle
import com.tachyonmusic.presentation.util.displayTitle

@Composable
fun HorizontalPlaybackView(
    playback: Playback,
    artwork: Artwork,
    isArtworkLoading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .shadow(Theme.shadow.extraSmall, shape = Theme.shapes.medium)
            .background(Theme.colors.secondary, shape = Theme.shapes.medium)
            .border(BorderStroke(1.dp, Theme.colors.border), shape = Theme.shapes.medium)
            .clickable { onClick() }
    ) {
        if (isArtworkLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(50.dp, 50.dp)
                    .clip(Theme.shapes.medium)
            )
        } else
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
                text = playback.displayTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2
            )

            Text(
                modifier = Modifier.padding(
                    start = Theme.padding.small,
                    bottom = Theme.padding.small
                ),
                text = playback.displaySubtitle,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
