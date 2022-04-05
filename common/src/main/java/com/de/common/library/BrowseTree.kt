package com.de.common.library

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.de.common.ext.artist
import com.de.common.ext.duration
import com.de.common.ext.id
import com.de.common.ext.title
import com.de.common.service.MediaPlaybackService
import java.io.File

class BrowseTree(
    val context: Context,
    musicSource: MusicSource,
    val recentMediaId: String? = null
) {
    companion object {
        /**
         * Defines the global root which is returned in [MediaPlaybackService.onGetRoot]
         */
        const val BROWSABLE_ROOT = "/"

        /**
         * If this is requested in [MediaPlaybackService.onLoadChildren] a list of all recently played
         * songs will be returned
         */
        const val HISTORY_ROOT = "__HISTORY__"
    }

    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    init {
        mediaIdToChildren[BROWSABLE_ROOT] = mutableListOf()
        mediaIdToChildren[BROWSABLE_ROOT]?.add(
            MediaMetadataCompat.Builder().apply {
                id = "/storage/emulated/0/Music/Brooklyn - Glockenbach.mp3"
                title = "Brooklyn"
                artist = "Glockenbach"
                duration = 100000

            }.build()
        )

        mediaIdToChildren[BROWSABLE_ROOT]?.add(
            MediaMetadataCompat.Builder().apply {
                id = "/storage/emulated/0/Music/Burn It All Down - League of Legends.mp3"
                title = "Burn It All Down"
                artist = "League of Legends"
                duration = 150000

            }.build()
        )

        mediaIdToChildren[BROWSABLE_ROOT]?.add(
            MediaMetadataCompat.Builder().apply {
                id = "\"/storage/emulated/0/Music/Last Time - Nerxa.mp3\""
                title = "Last Time"
                artist = "Nerxa"
                duration = 150000

            }.build()
        )
    }


    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]
}

