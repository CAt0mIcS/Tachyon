package com.example.mucify.device

import android.support.v4.media.MediaMetadataCompat

class BrowserTree(
    mediaSource: MediaSource
) {
    companion object {
        const val ROOT = "/"
    }

    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    operator fun get(parentId: String) = mediaIdToChildren[parentId]


    init {
        mediaIdToChildren[ROOT] = mutableListOf()

        mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                mediaSource.forEach { metadata ->
                    mediaIdToChildren[ROOT]!!.add(metadata)
                }
            }
        }
    }
}