package com.daton.media.device

import android.support.v4.media.MediaBrowserCompat
import com.daton.media.data.MediaId
import com.daton.media.playback.Playlist


class BrowserTree(
    private var mediaSource: MediaSource
) {
    companion object {
        /**
         * Use this to get all playbacks
         */
        const val ROOT = "/"

        const val SONG_ROOT = ROOT + "Song/"

        const val PLAYLIST_ROOT = ROOT + "Playlist/"

        const val LOOP_ROOT = ROOT + "Loop/"
    }

    operator fun get(parentId: String): List<MediaBrowserCompat.MediaItem>? {
        // TODO: Optimize

        // Basic predefined types
        when (parentId) {
            ROOT -> {
                return get(SONG_ROOT)!! + get(LOOP_ROOT)!! + get(PLAYLIST_ROOT)!!
            }
            SONG_ROOT -> {
                // TODO: Takes ages?
                return mediaSource.songs.map {
                    it.toMediaBrowserMediaItem()
                }
            }
            PLAYLIST_ROOT -> {
                return mediaSource.playlists.map {
                    it.toMediaBrowserMediaItem()
                }
            }
            LOOP_ROOT -> {
                return mediaSource.loops.map {
                    it.toMediaBrowserMediaItem()
                }
            }
        }

        /**
         * Assume that [parentId] is the id of a [Playback].
         * If the id points to a [Playlist], return the items in the playlist
         */
        val mediaId = MediaId.deserializeIfValid(parentId)
        if (mediaId != null) {
            val playback = mediaSource.findById(mediaId)
            if (playback is Playlist)
                return playback.toMediaBrowserMediaItemList()
        }

        return null
    }
}