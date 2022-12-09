package com.daton.database.data.repository

import android.util.Log
import androidx.paging.*
import com.daton.artworkfetcher.ArtworkFetcher
import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.ArtworkSource
import com.daton.database.domain.ArtworkType
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.net.URI

class SongRepositoryImpl(
    private val dao: SongDao,
    private val artworkSource: ArtworkSource,
    private val artworkFetcher: ArtworkFetcher
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        dao.getSongs().map { song ->
            LocalSongImpl(song.mediaId, song.title, song.artist, song.duration).apply {
                this.artwork.value = getArtworkForSong(song)
            }
        }

    override fun getPagedSongs(
        pageSize: Int,
        prefetchDistance: Int,
        initialLoadSize: Int
    ): Flow<PagingData<Song>> {
        val pagingSourceFactory = { dao.getPagedSongs() }
        return Pager(
            config = PagingConfig(pageSize, prefetchDistance, initialLoadSize = initialLoadSize),
            // TODO: Mediator (https://farhan-tanvir.medium.com/clean-architecture-in-android-jetpack-compose-paging-3-0-kotlin-mvvm-%E3%83%BCpart-2-8d97cee4dffe)
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { songData ->
            songData.map { song ->
                LocalSongImpl(song.mediaId, song.title, song.artist, song.duration).apply {
                    this.artwork.value = getArtworkForSong(song)
                }
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
            loadArtworkForSong(song)
        }
    }


    private suspend fun getArtworkForSong(song: SongEntity): Artwork? {
        return when (song.artworkType) {
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

    private suspend fun loadArtworkForSong(song: SongEntity) {
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