package com.tachyonmusic.media.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toPlaylist
import com.tachyonmusic.media.util.getItemsOnPageWithPageSize
import com.tachyonmusic.media.util.toMediaItems
import kotlinx.coroutines.*


class BrowserTree(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val playlistRepository: PlaylistRepository
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

    val root: MediaItem
        get() = MediaItem.Builder().apply {
            setMediaId(ROOT)
            setMediaMetadata(MediaMetadata.Builder().apply {
                setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                setIsPlayable(false)
            }.build())
        }.build()

    suspend fun get(parentId: String, page: Int, pageSize: Int): ImmutableList<MediaItem>? =
        withContext(Dispatchers.IO) {
            when (parentId) {
                ROOT -> constraintItems(getSongs() + getLoops() + getPlaylists(), page, pageSize)
                SONG_ROOT -> constraintItems(getSongs(), page, pageSize)
                LOOP_ROOT -> constraintItems(getLoops(), page, pageSize)
                PLAYLIST_ROOT -> constraintItems(getPlaylists(), page, pageSize)
                else -> {
                    /**
                     * Assume that [parentId] is the id of a [Playback].
                     * If the id points to a [Playlist], return the items in the playlist
                     */
                    val mediaId = MediaId.deserializeIfValid(parentId)
                    if (mediaId != null) {
                        val playback = playlistRepository.findByMediaId(mediaId)
                            ?.toPlaylist(songRepository, loopRepository)
                        if (playback != null)
                            return@withContext constraintItems(
                                playback.playbacks.toMediaItems(),
                                page,
                                pageSize
                            )
                    }

                    null
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


    // TODO: Nullable?
    private suspend fun getSongs() = songRepository.getSongs().map { it.toMediaItem() }
    private suspend fun getLoops() = loopRepository.getLoops().map { it.toMediaItem() }
    private suspend fun getPlaylists() = playlistRepository.getPlaylists().map { it.toMediaItem() }

    private fun constraintItems(
        playbacks: List<MediaItem>,
        page: Int,
        pageSize: Int
    ): ImmutableList<MediaItem> = getItemsOnPageWithPageSize(playbacks, page, pageSize)
}