package com.tachyonmusic.media.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.media.util.getItemsOnPageWithPageSize
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import kotlinx.coroutines.*


class BrowserTree(
    private val playbackRepository: PlaybackRepository,
) {
    companion object {
        /**
         * Use this to get all playbacks
         */
        const val ROOT = "/"

        const val SONG_ROOT = ROOT + "Song/"

        const val PLAYLIST_ROOT = ROOT + "Playlist/"

        const val LOOP_ROOT = ROOT + "Remix/"
    }

    var maximumRootChildLimit: Int = 4

    val root: MediaItem
        get() = MediaItem.Builder().apply {
            setMediaId(ROOT)
            setMediaMetadata(MediaMetadata.Builder().apply {
                setIsBrowsable(true)
                setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                setIsPlayable(false)
            }.build())
        }.build()

    fun get(parentId: String, page: Int, pageSize: Int): ImmutableList<MediaItem>? {
            return when (parentId) {
                ROOT -> {
                    ImmutableList.of(
                        MediaItem.Builder().apply {
                            setMediaId(SONG_ROOT)
                            setMediaMetadata(MediaMetadata.Builder().apply {
                                setIsBrowsable(true)
                                setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                                setIsPlayable(false)
                                setTitle("Songs") // TODO: Resources for titles v
                            }.build())
                        }.build(),

                        MediaItem.Builder().apply {
                            setMediaId(LOOP_ROOT)
                            setMediaMetadata(MediaMetadata.Builder().apply {
                                setIsBrowsable(true)
                                setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                                setIsPlayable(false)
                                setTitle("Remixes")
                            }.build())
                        }.build(),

                        MediaItem.Builder().apply {
                            setMediaId(PLAYLIST_ROOT)
                            setMediaMetadata(MediaMetadata.Builder().apply {
                                setIsBrowsable(true)
                                setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                                setIsPlayable(false)
                                setTitle("Playlists")
                            }.build())
                        }.build()
                    )
                }
                SONG_ROOT -> constraintItems(getSongs(), page, pageSize)
                LOOP_ROOT -> constraintItems(getRemixes(), page, pageSize)
                PLAYLIST_ROOT -> constraintItems(getPlaylists(), page, pageSize)
                else -> {
                    /**
                     * Assume that [parentId] is the id of a [Playback].
                     * If the id points to a [Playlist], return the items in the playlist
                     */
                    val mediaId = MediaId.deserializeIfValid(parentId)
                    if (mediaId != null) {
                        val playback =
                            playbackRepository.playlists.find { it.mediaId == mediaId }
                        if (playback != null)
                            return constraintItems(
                                playback.playbacks.map { it.toMediaItem() },
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
    private fun getSongs() = playbackRepository.songs.map { it.toMediaItem() }
    private fun getRemixes() =
        playbackRepository.remixes.map { it.toMediaItem() }

    private fun getPlaylists() = playbackRepository.playlists.map { it.toMediaItem() }

    private fun constraintItems(
        playbacks: List<MediaItem>,
        page: Int,
        pageSize: Int
    ): ImmutableList<MediaItem> = getItemsOnPageWithPageSize(playbacks, page, pageSize)
}