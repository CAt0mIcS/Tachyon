package com.tachyonmusic.media.domain.model

import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song

class TestSong(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Long
) : Song(mediaId, title, artist, duration) {
    override val playbackType = PlaybackType.Song.Local()
}