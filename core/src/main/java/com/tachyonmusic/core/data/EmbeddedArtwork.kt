package com.tachyonmusic.core.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import java.io.File


/**
 * Artwork that is embedded in the audio file
 */
class EmbeddedArtwork(
    val bitmap: ImageBitmap
) : Artwork {
    override val painter: Painter
        @Composable
        get() = remember(bitmap) { BitmapPainter(bitmap) }

    companion object {
        fun load(path: File) = SongMetadata.loadBitmap(path)?.asImageBitmap()
    }
}