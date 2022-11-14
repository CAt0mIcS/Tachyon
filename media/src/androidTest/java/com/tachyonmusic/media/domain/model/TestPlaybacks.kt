package com.tachyonmusic.media.domain.model

import android.net.Uri
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.data.playback.Song

class TestSong(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Long
) : Song(mediaId, title, artist, duration) {
    override val playbackType = PlaybackType.Song.Local()
    override val uri: Uri = Uri.fromFile(mediaId.path)
}