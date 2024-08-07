package com.tachyonmusic.presentation.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.presentation.core_components.AnimatedText
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun VerticalPlaybackView(
    playback: PlaybackUiEntity,
    artwork: Artwork,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(Theme.padding.extraSmall * 2 + 100.dp)
    ) {
        artwork.Image(
            contentDescription = "Album Artwork",
            modifier = Modifier
                .padding(Theme.padding.extraSmall)
                .size(100.dp, 100.dp)
                .clip(Theme.shapes.medium)
        )

        AnimatedText(
            modifier = Modifier
                .padding(start = Theme.padding.small, end = Theme.padding.small),
            text = playback.displayTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            gradientEdgeColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )

        AnimatedText(
            modifier = Modifier
                .padding(
                    start = Theme.padding.small * 2,
                    bottom = Theme.padding.small,
                    end = Theme.padding.small
                ),
            text = playback.displaySubtitle,
            fontSize = 12.sp,
            gradientEdgeColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}
