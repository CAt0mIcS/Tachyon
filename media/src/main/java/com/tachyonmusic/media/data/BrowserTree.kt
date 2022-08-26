package com.tachyonmusic.media.data

import androidx.media3.common.MediaItem
import com.google.common.collect.ImmutableList
import com.tachyonmusic.core.domain.model.MediaId
import com.tachyonmusic.core.domain.model.Playlist
import com.tachyonmusic.user.domain.FileRepository


class BrowserTree(
    private var repository: FileRepository
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

    operator fun get(parentId: String): ImmutableList<MediaItem>? {
        return when (parentId) {
            ROOT -> {
                return ImmutableList.copyOf(getSongs() + getLoops() + getPlaylists())
            }
            SONG_ROOT -> ImmutableList.copyOf(getSongs())
            PLAYLIST_ROOT -> ImmutableList.copyOf(getLoops())
            LOOP_ROOT -> ImmutableList.copyOf(getPlaylists())
            else -> {
                /**
                 * Assume that [parentId] is the id of a [Playback].
                 * If the id points to a [Playlist], return the items in the playlist
                 */
                val mediaId = MediaId.deserializeIfValid(parentId)
                if (mediaId != null) {
                    val playback = repository.find(mediaId)
                    if (playback != null && playback is Playlist)
                        return playback.toMediaItemList()
                }

                return null
            }
        }
    }

    // TODO
    /** Type for a folder containing only playable media.  */
    /** Type for a folder containing media categorized by album.  */
    /** Type for a folder containing media categorized by artist.  */
    /** Type for a folder containing media categorized by genre.  */
    /** Type for a folder containing a playlist.  */
    /** Type for a folder containing media categorized by year.  */


    private fun getSongs() = repository.songs.map { it.toMediaItem() }
    private fun getLoops() = repository.loops.map { it.toMediaItem() }
    private fun getPlaylists() = repository.playlists.map { it.toMediaItem() }
}