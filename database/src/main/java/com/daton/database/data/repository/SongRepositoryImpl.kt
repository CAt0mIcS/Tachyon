package com.daton.database.data.repository

import android.util.Log
import com.daton.artworkfetcher.ArtworkFetcher
import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.ArtworkSource
import com.daton.database.domain.ArtworkType
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.net.URI

class SongRepositoryImpl(
    private val dao: SongDao,
    private val artworkSource: ArtworkSource,
    private val artworkFetcher: ArtworkFetcher
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        dao.getSongs().map { song ->
            LocalSongImpl(song.mediaId, song.title, song.artist, song.duration).apply {
                this.artwork.value = when (song.artworkType) {
                    ArtworkType.NO_ARTWORK -> null
                    ArtworkType.EMBEDDED -> {
                        val path = song.mediaId.path
                        if (path == null) {
                            updateArtwork(song, ArtworkType.NO_ARTWORK, null)
                            null
                        } else {
                            val bitmap = EmbeddedArtwork.load(path)
                            if (bitmap == null) {
                                updateArtwork(song, ArtworkType.NO_ARTWORK, null)
                                null
                            } else
                                EmbeddedArtwork(bitmap)
                        }
                    }
                    ArtworkType.REMOTE -> {
                        if (song.artworkUrl.isNullOrBlank()) {
                            updateArtwork(song, ArtworkType.NO_ARTWORK, null)
                            null
                        } else
                            RemoteArtwork(URI(song.artworkUrl!!))
                    }
                    else -> TODO("Invalid artwork type ${song.artworkType}")
                }
            }
        }

    override suspend fun getSongEntities(): List<SongEntity> = dao.getSongs()

    override suspend fun removeIf(pred: (SongEntity) -> Boolean) {
        getSongEntities().forEach {
            if (pred(it))
                dao.delete(it)
        }
    }

    override suspend fun addAll(songs: List<SongEntity>) {
        dao.addAll(songs)
    }

    override suspend fun updateArtwork(song: SongEntity, artworkType: String, artworkUrl: String?) {
        dao.updateArtwork(song.id ?: return, artworkType, artworkUrl)
    }

    override suspend fun loadArtworks() {
        dao.getSongs().filter { it.artworkType == ArtworkType.NO_ARTWORK }.forEach { song ->
            when (val artwork = artworkSource.get(song)) {
                is RemoteArtwork -> updateArtwork(
                    song,
                    ArtworkType.REMOTE,
                    artwork.uri.toURL().toString()
                )
                is EmbeddedArtwork -> updateArtwork(song, ArtworkType.EMBEDDED)
                null -> {
                    /**
                     * We haven't yet found any artwork for this song, search the web if there's anything
                     * we can find
                     */
                    Log.d(
                        "SongRepositoryImpl",
                        "Searching web for artwork for ${song.title} - ${song.artist}"
                    )
                    artworkFetcher.query(song.title, song.artist, 1000)
                        .onEach { res ->
                            if (res is Resource.Success)
                                updateArtwork(song, ArtworkType.REMOTE, res.data ?: return@onEach)
                        }.collect()
                }
                else -> TODO("Unknown artwork type")
            }
        }
    }
}