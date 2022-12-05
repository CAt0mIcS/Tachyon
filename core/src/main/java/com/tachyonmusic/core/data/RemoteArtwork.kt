package com.tachyonmusic.core.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import coil.compose.rememberAsyncImagePainter
import com.tachyonmusic.core.domain.Artwork
import java.net.URI


/**
 * Artwork which can be downloaded using a link
 */
class RemoteArtwork(
    val uri: URI
) : Artwork {
    override val painter: Painter
        @Composable
        get() = rememberAsyncImagePainter(uri)
}