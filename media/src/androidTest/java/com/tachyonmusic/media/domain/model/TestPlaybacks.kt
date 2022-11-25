package com.tachyonmusic.media.domain.model

import android.net.Uri
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.data.SongMetadata
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import java.io.File

class TestSong(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Long
) : Song(mediaId, title, artist, duration) {

    override val playbackType = PlaybackType.Song.Local()
    override val uri: Uri = Uri.fromFile(mediaId.path)

    val path: File
        get() = mediaId.path!!

    override suspend fun loadBitmap(onDone: suspend () -> Unit) {
        if (artwork == null)
            artwork = SongMetadata.loadBitmap(path)
        onDone()
    }
}