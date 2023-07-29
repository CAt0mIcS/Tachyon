package com.tachyonmusic.domain

import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.logger.domain.Logger

/**
 * Loads or unloads artwork for a playback depending on the specified range
 */
class LoadArtworkForPlayback(
    private val metadataExtractor: SongMetadataExtractor,
    private val log: Logger
) {
    @JvmName("invokeSongs")
    operator fun invoke(songs: List<Song>, range: IntRange): List<Song> {
        return songs.mapIndexed { i, song ->
            if (i in range && song.artwork is EmbeddedArtwork && !song.artwork!!.isLoaded) {
                // Load artwork
                val uri = (song.artwork as EmbeddedArtwork).uri
                song.artwork = EmbeddedArtwork(EmbeddedArtwork.load(uri, metadataExtractor), uri)
                log.debug("Loading artwork for $song")
            } else if (i !in range && song.artwork is EmbeddedArtwork && song.artwork!!.isLoaded) {
                // Unload artwork
                song.artwork = EmbeddedArtwork(null, (song.artwork as EmbeddedArtwork).uri)
                log.debug("Unloading artwork for $song")
            }
            song
        }
    }

    @JvmName("invokeCustomizedSongs")
    operator fun invoke(
        customizedSongs: List<CustomizedSong>,
        range: IntRange
    ): List<CustomizedSong> {
        val newSongs = invoke(customizedSongs.map { it.song }, range)
        return customizedSongs.onEachIndexed { i, customized ->
            customized.artwork = newSongs[i].artwork
        }
    }

    @JvmName("invokePlaylists")
    operator fun invoke(playlists: List<Playlist>, range: IntRange): List<Artwork?> {
        return playlists.mapIndexed { i, playlist ->
            val artwork = if (i in range) {
                log.debug("Loading artwork for $playlist")

                val itemWithArtwork = playlist.playbacks.find { it.hasArtwork }
                if (itemWithArtwork == null)
                    null
                else
                    invoke(itemWithArtwork).artwork
            } else if (i !in range) {
                log.debug("Unloading artwork for $playlist")

                null
            } else null
            artwork
        }
    }

    @JvmName("invokeSinglePlayback")
    operator fun invoke(playback: SinglePlayback) = when (playback) {
        is Song -> invoke(playback)
        is CustomizedSong -> invoke(playback)
        else -> TODO("Invalid playback type")
    }

    /**
     * @return song with loaded artwork
     */
    @JvmName("invokeSong")
    operator fun invoke(song: Song): Song {
        if (song.artwork == null || song.artwork!!.isLoaded || song.artwork !is EmbeddedArtwork)
            return song

        val uri = (song.artwork as EmbeddedArtwork).uri
        song.artwork = EmbeddedArtwork(EmbeddedArtwork.load(uri, metadataExtractor), uri)
        return song
    }

    /**
     * @return customized song with loaded artwork for underlying song
     */
    @JvmName("invokeCustomizedSong")
    operator fun invoke(customized: CustomizedSong): CustomizedSong {
        customized.artwork = invoke(customized.song).artwork
        return customized
    }
}