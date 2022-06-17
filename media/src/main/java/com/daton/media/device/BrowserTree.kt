package com.daton.media.device

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.daton.media.ext.*

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
                return mediaSource.songs.map { it.toMediaBrowserMediaItem() }
            }
            PLAYLIST_ROOT -> {
                return mediaSource.playlists.map { it.toMediaMetadata().toMediaBrowserMediaItem() }
            }
            LOOP_ROOT -> {
                return mediaSource.loops.map {
                    it.toMediaMetadata(mediaSource).toMediaBrowserMediaItem()
                }
            }
        }

        /**
         * Assume that [parentId] is a Json-serialized [MediaId].
         * If that media id is valid and is a playlist we should return all items in the playlist
         */
        val mediaId = MediaId.deserializeIfValid(parentId)
        if (mediaId != null && mediaId.isPlaylist) {
            return mediaSource.findPlaylist { it.mediaId == mediaId }
                ?.toMediaMetadataList(mediaSource)?.map { it.toMediaBrowserMediaItem() }
        }

        return null
    }
}