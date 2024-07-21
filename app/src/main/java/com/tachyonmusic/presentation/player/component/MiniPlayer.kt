package com.tachyonmusic.presentation.player.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.presentation.core_components.AnimatedText
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.theme.interpolate

@Composable
fun MiniPlayer(
    playback: PlaybackUiEntity?,
    currentPosition: Float,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayPauseClicked: () -> Unit,
    artwork: Artwork,
    modifier: Modifier = Modifier
) {
    if (playback == null)
        return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Theme.padding.extraSmall,
                end = Theme.padding.extraSmall
            )
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = Theme.shapes.medium
            )
            .clickable {
                onClick()
            }
    ) {
        Row {
            val artworkModifier = Modifier
                .padding(Theme.padding.extraSmall)
                .size(48.dp) // TODO Dynamic
                .clip(Theme.shapes.medium)


            artwork(contentDescription = "Album Artwork", modifier = artworkModifier)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
                    .padding(start = Theme.padding.small),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedText(
                        modifier = Modifier
                            .padding(top = Theme.padding.small),
                        text = playback.displayTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        gradientEdgeColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    AnimatedText(
                        modifier = Modifier
                            .padding(start = Theme.padding.small, bottom = Theme.padding.small),
                        text = playback.displaySubtitle,
                        fontSize = 12.sp,
                        gradientEdgeColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }

                IconButton(
                    modifier = Modifier
                        .padding(start = Theme.padding.medium, end = Theme.padding.medium)
                        .align(Alignment.CenterVertically)
                        .scale(1.6f), // TODO dynamic
                    onClick = onPlayPauseClicked
                ) {
                    Icon(
                        painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = "Play/Pause"
                    )
                }
            }
        }

        ProgressIndicator(
            progress = currentPosition,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Theme.padding.medium, end = Theme.padding.medium),
            color = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.interpolate(MaterialTheme.colorScheme.onPrimaryContainer)
                .interpolate(MaterialTheme.colorScheme.background)
        )
    }
}