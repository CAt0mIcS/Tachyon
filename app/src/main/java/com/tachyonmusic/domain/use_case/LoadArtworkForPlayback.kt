package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Remix
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
    operator fun invoke(songs: List<Song>, range: IntRange, quality: Int = 100): List<Song> {
        return songs.mapIndexed { i, song ->

            if (i in range && song.artwork is EmbeddedArtwork && !song.artwork!!.isLoaded) {
                // Load artwork
                val uri = (song.artwork as EmbeddedArtwork).uri
                song.artwork =
                    EmbeddedArtwork(EmbeddedArtwork.load(uri, metadataExtractor, quality), uri)
                log.debug("Loading artwork for $song")
            } else if (i !in range && song.artwork is EmbeddedArtwork && song.artwork!!.isLoaded) {
                // Unload artwork
                song.artwork = EmbeddedArtwork(null, (song.artwork as EmbeddedArtwork).uri)
                log.debug("Unloading artwork for $song")
            }
            song
        }
    }

    @JvmName("invokeRemixes")
    operator fun invoke(
        remixes: List<Remix>,
        range: IntRange,
        quality: Int = 100
    ): List<Remix> {
        val newSongs = invoke(remixes.map { it.song }, range, quality)
        return remixes.onEachIndexed { i, remix ->
            remix.artwork = newSongs[i].artwork
        }
    }

    @JvmName("invokePlaylists")
    operator fun invoke(
        playlists: List<Playlist>,
        range: IntRange,
        quality: Int = 100
    ): List<Playlist> {
        return playlists.mapIndexed { i, playlist ->
            if (i in range) {
                log.debug("Loading artwork for $playlist")

                val itemWithArtwork = playlist.playbacks.find { it.hasArtwork }
                if (itemWithArtwork == null)
                    playlist
                else {
                    itemWithArtwork.artwork = invoke(itemWithArtwork, quality).artwork
                    playlist
                }
            } else if (i !in range) {
                log.debug("Unloading artwork for $playlist")
                val itemWithArtwork = playlist.playbacks.find { it.hasArtwork }
                itemWithArtwork?.artwork = null
                playlist
            } else playlist
        }
    }

    @JvmName("invokeSinglePlaybacks")
    operator fun invoke(
        playbacks: List<Playback>,
        range: IntRange,
        quality: Int = 100
    ): List<Playback> {
        val songs = invoke(playbacks.map { playback ->
            when (playback) {
                is SinglePlayback -> playback.underlyingSong
                is Playlist -> (playback.playbacks.find { it.hasArtwork }
                    ?: playback.playbacks.first()).underlyingSong

                else -> TODO("Invalid playback type")
            }
        }, range, quality)
        return playbacks.onEachIndexed { i, it ->
            when (it) {
                is SinglePlayback -> it.artwork = songs[i].artwork
                is Playlist -> (it.playbacks.find { it.hasArtwork }
                    ?: it.playbacks.first()).artwork = songs[i].artwork
            }
        }
    }

    @JvmName("invokeSinglePlayback")
    operator fun invoke(playback: SinglePlayback, quality: Int = 100) = when (playback) {
        is Song -> invoke(playback, quality)
        is Remix -> invoke(playback, quality)
        else -> TODO("Invalid playback type")
    }

    /**
     * @return song with loaded artwork
     */
    @JvmName("invokeSong")
    operator fun invoke(song: Song, quality: Int = 100): Song {
        if (song.artwork == null || song.artwork!!.isLoaded || song.artwork !is EmbeddedArtwork)
            return song

        val uri = (song.artwork as EmbeddedArtwork).uri
        song.artwork = EmbeddedArtwork(EmbeddedArtwork.load(uri, metadataExtractor, quality), uri)
        return song
    }

    /**
     * @return remix with loaded artwork for underlying song
     */
    @JvmName("invokeRemix")
    operator fun invoke(remix: Remix, quality: Int = 100): Remix {
        remix.artwork = invoke(remix.song, quality).artwork
        return remix
    }
}