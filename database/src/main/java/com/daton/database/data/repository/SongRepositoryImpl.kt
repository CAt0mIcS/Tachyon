package com.daton.database.data.repository

import android.util.Log
import com.daton.artworkfetcher.ArtworkFetcher
import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.ArtworkSource
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

class SongRepositoryImpl(
    private val dao: SongDao,
    private val artworkSource: ArtworkSource,
    private val artworkFetcher: ArtworkFetcher
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        dao.getSongs().map { song ->
            LocalSongImpl(song.mediaId, song.title, song.artist, song.duration).apply {
                val artwork = artworkSource.get(song)
                if (artwork != null)
                    this.artwork.value = artwork
                else {
                    Log.d("SongRepositoryImpl", "Searching web for artwork for $title - $artist")
                    artworkFetcher.query(title, artist, 1000)
                        .onEach { res ->
                            if(res is Resource.Success) {
                                updateArtwork(song, res.data)
                                this.artwork.value = artworkSource.get(song.let {
                                    it.artwork = res.data
                                    return@let it
                                })
                            }
                        }.collect()
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

    override suspend fun updateArtwork(song: SongEntity, artwork: String?) {
        dao.updateArtwork(song.id ?: return, artwork)
    }
}


// TODO: AlbumArt
private fun Song.toEntity() = SongEntity(
    mediaId, title, artist, duration
)