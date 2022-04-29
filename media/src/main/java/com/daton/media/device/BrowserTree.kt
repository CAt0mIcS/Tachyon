package com.daton.media.device

import android.support.v4.media.MediaMetadataCompat
import com.daton.media.ext.*

class BrowserTree(
    mediaSource: MediaSource
) {
    companion object {
        /**
         * Use this to get all available media items (Songs, Loops, and Playlists)
         */
        const val ROOT = "/"

        /**
         * Use this to only get songs
         */
        const val SONG_ROOT = "/SONG"

        /**
         * Use this to only get loops
         */
        const val LOOP_ROOT = "/LOOP"

        /**
         * Use this to only get playlists
         */
        const val PLAYLIST_ROOT = "/PLAYLIST"
    }

    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    operator fun get(parentId: String): MutableList<MediaMetadataCompat>? {
        return if (parentId == ROOT) {
            // Concatenate all different playback types
            mutableListOf<MediaMetadataCompat>().apply {
                mediaIdToChildren[SONG_ROOT]?.let { addAll(it) }
                mediaIdToChildren[LOOP_ROOT]?.let { addAll(it) }
                mediaIdToChildren[PLAYLIST_ROOT]?.let { addAll(it) }
            }
        } else
            mediaIdToChildren[parentId]
    }


    init {
        mediaIdToChildren[ROOT] = mutableListOf()

        mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                mediaSource.forEach { metadata ->
                    if (metadata.isSong) {
                        if (mediaIdToChildren[SONG_ROOT] == null)
                            mediaIdToChildren[SONG_ROOT] = mutableListOf()
                        mediaIdToChildren[SONG_ROOT]!!.add(metadata)
                    } else if (metadata.isLoop) {
                        if (mediaIdToChildren[LOOP_ROOT] == null)
                            mediaIdToChildren[LOOP_ROOT] = mutableListOf()
                        mediaIdToChildren[LOOP_ROOT]!!.add(metadata)

                        // Add underlying song to loop
                        mediaIdToChildren[metadata.mediaId] =
                            mutableListOf(MediaSource.loadSong(metadata.path))
                    } else if (metadata.isPlaylist) {
                        if (mediaIdToChildren[PLAYLIST_ROOT] == null)
                            mediaIdToChildren[PLAYLIST_ROOT] = mutableListOf()
                        mediaIdToChildren[PLAYLIST_ROOT]!!.add(metadata)

                        // Add all songs in the playlist
                        mediaIdToChildren[metadata.mediaId] =
                            MediaSource.loadPlaylist(metadata.path)
                    } else
                        TODO("Metadata audio type not set")
                }
            }
        }
    }
}