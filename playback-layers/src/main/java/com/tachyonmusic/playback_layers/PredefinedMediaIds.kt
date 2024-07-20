package com.tachyonmusic.playback_layers

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist

val predefinedRemixPlaylistMediaId =
    MediaId.ofLocalPlaylist("com.tachyonmusic.PREDEFINED_LOOPS_PLAYLIST")

val predefinedSongPlaylistMediaId =
    MediaId.ofLocalPlaylist("com.tachyonmusic.PREDEFINED_SONGS_PLAYLIST")

val Playlist.isPredefined: Boolean
    get() = mediaId == predefinedSongPlaylistMediaId || mediaId == predefinedRemixPlaylistMediaId