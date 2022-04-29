package com.daton.media

import android.support.v4.media.MediaMetadataCompat

object MetadataKeys {
    const val MediaUri = MediaMetadataCompat.METADATA_KEY_MEDIA_URI
    const val Duration = MediaMetadataCompat.METADATA_KEY_DURATION
    const val Title = MediaMetadataCompat.METADATA_KEY_TITLE
    const val Artist = MediaMetadataCompat.METADATA_KEY_ARTIST
    const val AlbumArt = MediaMetadataCompat.METADATA_KEY_ALBUM_ART
    const val StartTime = "com.de.mucify.START_POS"
    const val EndTime = "com.de.mucify.END_POS"
    const val MediaId = MediaMetadataCompat.METADATA_KEY_MEDIA_ID
}