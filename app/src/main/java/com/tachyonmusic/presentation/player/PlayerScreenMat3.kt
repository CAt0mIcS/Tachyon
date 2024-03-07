package com.tachyonmusic.presentation.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.MotionLayoutScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.presentation.core_components.AnimatedText
import com.tachyonmusic.presentation.entry.LayoutId
import com.tachyonmusic.presentation.player.component.ProgressIndicator
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun MotionLayoutScope.PlayerScreenMat3(
    viewModel: MiniPlayerViewModel = hiltViewModel()
) {
    val playback by viewModel.playback.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

//      TODO MAT3: Hide miniplayer completely when playback is null
//    if (playback == null)
//        return

    Box(modifier = Modifier.layoutId("PrimaryArtwork")) {
        (playback?.artwork ?: PlaceholderArtwork).Image(
            contentDescription = "Album Artwork",
            modifier = Modifier
                .clip(Theme.shapes.medium)
                .layoutId(LayoutId.PrimaryArtwork)
                .aspectRatio(1f)
        )
    }

    Text(
        modifier = Modifier.layoutId(LayoutId.PrimaryTitle),
        text = playback?.displayTitle ?: "Unknown",
        fontWeight = FontWeight.Bold,
        fontSize = motionFontSize(LayoutId.PrimaryTitle, "fontSize"),
//        gradientEdgeColor = Theme.colors.tertiary,
        textAlign = TextAlign.Left
    )

    Text(
        modifier = Modifier.layoutId(LayoutId.PrimarySubtitle),
        text = playback?.displaySubtitle ?: "Unknown",
        fontSize = motionFontSize(LayoutId.PrimarySubtitle, "fontSize"),
//        gradientEdgeColor = Theme.colors.tertiary,
        textAlign = TextAlign.Left
    )

    IconButton(
        modifier = Modifier.layoutId(LayoutId.PlayPauseButton),
        onClick = viewModel::pauseResume
    ) {
        Icon(
            painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = "Play/Pause"
        )
    }
}