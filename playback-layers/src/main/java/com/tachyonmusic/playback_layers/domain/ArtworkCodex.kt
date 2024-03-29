package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface ArtworkCodex {

    data class ArtworkUpdateData(
        val artwork: Artwork? = null,
        val entityToUpdate: SongEntity? = null
    )

    /**
     * Tries to either wait for artwork to be loaded if [awaitOrLoad] was called for [entity.mediaId]
     * somewhere else OR loads it if [awaitOrLoad] hasn't been called OR returns if the artwork
     * was already loaded. If the returned [Resource] is [Resource.Success] it is save to call [get]
     * to get the now loaded artwork
     *
     * @param fetchOnline controls whether the [ArtworkFetcher] runs if no local artwork is found
     */
    suspend fun awaitOrLoad(
        entity: SongEntity,
        fetchOnline: Boolean = true
    ): Flow<Resource<ArtworkUpdateData>>

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

    /**
     * Gets artwork that is already cached in the database (SongEntity has any [ArtworkType] other
     * than [ArtworkType.UNKNOWN]). May return an entity to update if cached artwork is invalid
     */
    suspend fun loadExisting(entity: SongEntity): ArtworkUpdateData

    fun isLoaded(mediaId: MediaId): Boolean
}