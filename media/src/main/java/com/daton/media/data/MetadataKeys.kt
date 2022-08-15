package com.daton.media.data

import android.support.v4.media.MediaMetadataCompat

object MetadataKeys {
    const val MediaUri = MediaMetadataCompat.METADATA_KEY_MEDIA_URI
    const val Duration = MediaMetadataCompat.METADATA_KEY_DURATION
    const val Title = MediaMetadataCompat.METADATA_KEY_TITLE
    const val Artist = MediaMetadataCompat.METADATA_KEY_ARTIST
    const val AlbumArt = MediaMetadataCompat.METADATA_KEY_ALBUM_ART
    const val Playback = "com.daton.mucify.PLAYBACK"
    const val StartTime = "com.daton.mucify.START_POS"
    const val EndTime = "com.daton.mucify.END_POS"
    const val MediaId = MediaMetadataCompat.METADATA_KEY_MEDIA_ID
    const val LoopName: String = "com.daton.mucify.LOOP_NAME"
    const val PlaylistPlaybacks = "com.daton.mucify.PLAYLIST_PLAYBACKS"
    const val CurrentPlaylistPlaybackIndex = "com.daton.mucify.CURRENT_PLAYLIST_PLAYBACK_INDEX"
}