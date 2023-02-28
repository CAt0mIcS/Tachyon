package com.tachyonmusic.media.util

import android.net.Uri
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.playback.AbstractLoop
import com.tachyonmusic.core.data.playback.AbstractPlaylist
import com.tachyonmusic.core.data.playback.AbstractSong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.File

internal class TestSong(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Duration
) : AbstractSong(mediaId, title, artist, duration) {

    override val playbackType = PlaybackType.Song.Local()
    override val uri: Uri = Uri.fromFile(mediaId.uri!!.raw)

    val path: File
        get() = mediaId.uri!!
}

internal class TestLoop(mediaId: MediaId, name: String, song: Song) :
    AbstractLoop(mediaId, name, TimingDataController(), song) {
    override val playbackType = PlaybackType.Loop.Remote()
}

internal class TestPlaylist(mediaId: MediaId, name: String, playbacks: List<SinglePlayback>) :
    AbstractPlaylist(mediaId, name, playbacks.toMutableList()) {
    override val playbackType = PlaybackType.Playlist.Remote()
}