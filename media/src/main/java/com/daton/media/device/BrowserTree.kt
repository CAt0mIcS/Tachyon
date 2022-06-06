package com.daton.media.device

import android.support.v4.media.MediaMetadataCompat
import com.daton.media.ext.*

class BrowserTree(
    mediaSource: MediaSource
) {
    companion object {
        /**
         * Use this to get all songs
         */
        const val ROOT = "/"
    }

    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    operator fun get(parentId: String): MutableList<MediaMetadataCompat>? =
        mediaIdToChildren[parentId]

    /**
     * Resets the entire [BrowserTree] and reloads from the media source
     */
    fun reload(mediaSource: MediaSource) {
        mediaIdToChildren[ROOT] = mutableListOf()
        mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                mediaSource.forEach { metadata ->
                    mediaIdToChildren[ROOT]!!.add(metadata)
                }
            } else
                TODO("MediaSource initialization unsuccessful")
        }
    }


    init {
        reload(mediaSource)
    }
}