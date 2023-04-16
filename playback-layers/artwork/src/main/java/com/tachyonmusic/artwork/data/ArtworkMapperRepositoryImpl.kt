package com.tachyonmusic.artwork.data

import com.tachyonmusic.artwork.*
import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.artwork.domain.ArtworkMapperRepository
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.sort.domain.SortedPlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


class ArtworkMapperRepositoryImpl(
    private val sortedPlaybackRepository: SortedPlaybackRepository,
    private val artworkCodex: ArtworkCodex
) : ArtworkMapperRepository {

    private val reloadPlaybacks = MutableStateFlow(false)

    override val songFlow =
        combine(sortedPlaybackRepository.songFlow, reloadPlaybacks) { songs, _ ->
            transformSongs(songs)
        }

    override val customizedSongFlow =
        combine(sortedPlaybackRepository.customizedSongFlow, reloadPlaybacks) { customizedSongs, _ ->
            transformCustomizedSongs(customizedSongs)
        }

    override val playlistFlow =
        combine(sortedPlaybackRepository.playlistFlow, reloadPlaybacks) { playlists, _ ->
            transformPlaylists(playlists)
        }

    override val historyFlow =
        combine(sortedPlaybackRepository.historyFlow, reloadPlaybacks) { history, _ ->
            transformHistory(history)
        }

    override suspend fun getSongs() = withContext(Dispatchers.IO) {
        transformSongs(sortedPlaybackRepository.getSongs())
    }

    override suspend fun getCustomizedSongs() = withContext(Dispatchers.IO) {
        transformCustomizedSongs(sortedPlaybackRepository.getCustomizedSongs())
    }

    override suspend fun getPlaylists() = withContext(Dispatchers.IO) {
        transformPlaylists(sortedPlaybackRepository.getPlaylists())
    }

    override suspend fun getHistory() = withContext(Dispatchers.IO) {
        transformHistory(sortedPlaybackRepository.getHistory())
    }


    override fun triggerPlaybackReload() {
        reloadPlaybacks.update { !it }
    }


    private fun transformSongs(songs: List<Song>) =
        songs.onEach { song ->
            song.applyArtwork()
        }

    private fun transformCustomizedSongs(customizedSongs: List<CustomizedSong>) =
        customizedSongs.onEach { customizedSong ->
            customizedSong.applyArtwork()
        }

    /**
     * OPTIMIZE: Is it faster to always get [songs] and [customizedSongs] or is it faster
     *  to just load the artwork twice. Same goes for [transformHistory]
     */
    private fun transformPlaylists(
        playlists: List<Playlist>
    ) = playlists.onEach { playlist ->
        playlist.playbacks.forEach {
            it.applyArtwork()
        }
    }

    private fun transformHistory(
        history: List<SinglePlayback>
    ) = history.onEach {
        it.applyArtwork()
    }


    private fun SinglePlayback.applyArtwork() {
        when (this) {
            is Song -> applyArtwork()
            is CustomizedSong -> song.applyArtwork()
            else -> error("Invalid single playback type ${this::class.java.name}")
        }
    }

    private fun Song.applyArtwork() {
        if (artworkCodex.isLoaded(mediaId)) {
            artwork = artworkCodex[mediaId]
            isArtworkLoading = false
        } else
            isArtworkLoading = true
    }
}