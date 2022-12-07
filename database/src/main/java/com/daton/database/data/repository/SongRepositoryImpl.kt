package com.daton.database.data.repository

import com.daton.artworkdownloader.ArtworkDownloader
import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.ArtworkSource
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.CachedArtwork
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.net.URI

class SongRepositoryImpl(
    private val dao: SongDao,
    private val artworkSource: ArtworkSource,
    private val artworkDownloader: ArtworkDownloader
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        dao.getSongs().map { song ->
            LocalSongImpl(song.mediaId, song.title, song.artist, song.duration).apply {
                val artwork = artworkSource.get(song)
                if (artwork != null)
                    this.artwork.value = artwork
                else {
                    artworkDownloader.query(title, artist, 1000)
                        .onEach { res ->
                            when (res) {
                                is Resource.Loading -> {}
                                is Resource.Error -> {
                                    // No artwork present for playback
                                    // Set artwork to empty string to show that we already checked the web
                                    // for cover art to download, but found nothing.
                                    updateArtwork(song, "")
                                }
                                is Resource.Success -> {
                                    updateArtwork(song, res.data)
                                    this.artwork.value = artworkSource.get(song.let {
                                        it.artwork = res.data
                                        return@let it
                                    })
                                }
                            }
                        }.collect()
                }
            }
        }

    override suspend fun removeIf(pred: (Song) -> Boolean) {
        getSongs().forEach {
            if (pred(it))
                dao.delete(it.toEntity())
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