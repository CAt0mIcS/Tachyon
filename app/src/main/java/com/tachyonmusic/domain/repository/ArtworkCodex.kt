package com.tachyonmusic.domain.repository

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.Resource

interface ArtworkCodex {
    /**
     * Tries to find the correct artwork type by first checking if it has embedded artwork inside
     * the .mp3 music file. If it doesn't find any it will use the artwork fetcher to find artwork
     * on the internet
     * @return either the song entity with new artwork type or artwork url OR null if no changes were
     * made to the initial [song] parameter
     */
    suspend fun requestLoad(song: SongEntity): Resource<SongEntity?>

    /**
     * Gets already loaded artwork by media id
     * @throws NoSuchElementException if the key [mediaId] doesn't exist in the codex
     */
    operator fun get(mediaId: MediaId): Artwork?

    fun isLoaded(mediaId: MediaId): Boolean
}