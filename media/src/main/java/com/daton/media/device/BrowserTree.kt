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

        const val PLAYLIST_ROOT = ROOT + "Playlist/"
    }

    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()
    private val playlistsToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    operator fun get(parentId: String): MutableList<MediaMetadataCompat>? {
        if (parentId == ROOT) {
            return mediaIdToChildren[ROOT]
        } else if (parentId == PLAYLIST_ROOT) {
            val metadata = arrayListOf<MediaMetadataCompat>()
            for (key in playlistsToChildren.keys)
                if (key.toMediaId().isPlaylist)
                    metadata += playlistsToChildren[key]!!
            return metadata
        }
        return mediaIdToChildren[parentId]
    }


    /**
     * Resets the entire [BrowserTree] and reloads from the media source
     */
    fun reload(mediaSource: MediaSource) {
        mediaIdToChildren[ROOT] = mutableListOf()

        mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                mediaSource.forEachSong { songMetadata ->
                    mediaIdToChildren[ROOT]!! += songMetadata
                }
                mediaSource.forEachLoop { loop ->
                    mediaIdToChildren[ROOT]!! += loop.toMediaMetadata(mediaSource)
                }
                mediaSource.forEachPlaylist { playlist ->
                    playlistsToChildren[playlist.mediaId.toString()] = mutableListOf()
                    playlistsToChildren[playlist.mediaId.toString()]!! += playlist.toMediaMetadataList(
                        mediaSource
                    )
                }
            } else
                TODO("MediaSource initialization unsuccessful")
        }
    }


    init {
        reload(mediaSource)
    }
}