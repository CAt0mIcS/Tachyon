package com.daton.media.data

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File

class SongMetadata(path: File) {

    val title: String
    val artist: String
    var albumArt: Bitmap? = null
        private set
    val duration: Long

    init {
        val metaRetriever = MediaMetadataRetriever()
        try {
            metaRetriever.setDataSource(path.absolutePath)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TODO("Implement error handling: ${path.absolutePath}")
        }

        title =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: path.nameWithoutExtension

        artist =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"

        // TODO: Decide if should be stored for all songs or loaded only when required
//        val art: ByteArray? = metaRetriever.embeddedPicture
//        if (art != null) {
//            albumArt = BitmapFactory.decodeByteArray(art, 0, art.size)
//        }

        duration =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toLong()
    }

}