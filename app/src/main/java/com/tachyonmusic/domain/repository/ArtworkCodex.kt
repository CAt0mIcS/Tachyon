package com.tachyonmusic.domain.repository

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.util.Resource

interface ArtworkCodex {
    /**
     * Tries to find the correct artwork type by first checking if it has embedded artwork inside
     * the .mp3 music file. If it doesn't find any it will use the artwork fetcher to find artwork
     * on the internet
     * @return either the song entity with new artwork type or artwork url OR null if no changes were
     * made to the initial [entity] parameter
     */
//    suspend fun requestLoad(entity: SinglePlaybackEntity): Resource<SinglePlaybackEntity?>

    /**
     * Tries to either wait for artwork to be loaded if [awaitOrLoad] was called for [entity.mediaId]
     * somewhere else OR loads it if [awaitOrLoad] hasn't been called OR returns if the artwork
     * was already loaded. If the returned [Resource] is [Resource.Success] it is save to call [get]
     * to get the now loaded artwork
     */
    suspend fun awaitOrLoad(entity: SinglePlaybackEntity): Resource<SinglePlaybackEntity?>
    suspend fun awaitOrLoad(
        mediaId: MediaId,
        artworkType: String,
        artworkUrl: String? = null
    ): Resource<SinglePlaybackEntity?>

    /**
     * Waits for artwork to be loaded if [awaitOrLoad] was called for [mediaId] or immediately returns
     * if [awaitOrLoad] hasn't been called for [mediaId]
     */
    suspend fun await(mediaId: MediaId)

    /**
     * Gets already loaded artwork by media id
     * @throws NoSuchElementException if the key [mediaId] doesn't exist in the codex
     */
    operator fun get(mediaId: MediaId): Artwork?

    /**
     * Gets already loaded artwork by media id or null if the artwork is null or doesn't exist
     */
    fun getOrNull(mediaId: MediaId): Artwork?

    fun isLoaded(mediaId: MediaId): Boolean
}