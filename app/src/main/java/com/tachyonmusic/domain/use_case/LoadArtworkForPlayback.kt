package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.indexOf
import com.tachyonmusic.util.replaceAt

/**
 * Loads or unloads artwork for a playback depending on the specified range
 */
class LoadArtworkForPlayback(
    private val metadataExtractor: SongMetadataExtractor,
    private val log: Logger
) {
    @JvmName("invokePlaybacks")
    operator fun invoke(
        playbacks: List<Playback>,
        range: IntRange,
        quality: Int = 100
    ): List<Playback> {
        assert(playbacks.all { it.isSong || it.isRemix })

        return playbacks.mapIndexed { i, pb ->
            val artwork =
                if (i in range && pb.artwork is EmbeddedArtwork && !pb.artwork!!.isLoaded) {
                    // Load artwork
                    val uri = (pb.artwork as EmbeddedArtwork).uri
                    log.debug("Loading artwork for $pb")
                    EmbeddedArtwork(EmbeddedArtwork.load(uri, metadataExtractor, quality), uri)
                } else if (i !in range && pb.artwork is EmbeddedArtwork && pb.artwork!!.isLoaded) {
                    // Unload artwork
                    log.debug("Unloading artwork for $pb")
                    EmbeddedArtwork(null, (pb.artwork as EmbeddedArtwork).uri)
                } else null

            pb.copy(artwork = artwork ?: return@mapIndexed pb, isArtworkLoading = false)
        }
    }

    @JvmName("invokePlaylists")
    operator fun invoke(
        playlists: List<Playlist>,
        range: IntRange,
        quality: Int = 100
    ): List<Playlist> {
        return playlists.mapIndexed { i, playlist ->
            when (i) {
                in range -> {
                    log.debug("Loading artwork for $playlist")

                    val iWithArtwork = playlist.playbacks.indexOf { it.hasArtwork }
                        ?: return@mapIndexed playlist
                    playlist.copy(
                        playbacks = playlist.playbacks.toMutableList().replaceAt(
                            iWithArtwork,
                            invoke(playlist.playbacks[iWithArtwork], quality)
                        )
                    )
                }
                !in range -> {
                    log.debug("Unloading artwork for $playlist")
                    val iWithArtwork = playlist.playbacks.indexOf { it.hasArtwork }
                        ?: return@mapIndexed playlist
                    playlist.copy(
                        playbacks = playlist.playbacks.toMutableList().replaceAt(
                            iWithArtwork,
                            playlist.playbacks[iWithArtwork].copy(artwork = null)
                        )
                    )
                }
                else -> playlist
            }
        }
    }

    @JvmName("invokePlayback")
    operator fun invoke(playback: Playback, quality: Int = 100) =
        invoke(listOf(playback), 0..Int.MAX_VALUE, quality)[0]

    @JvmName("invokePlaylists")
    operator fun invoke(playlist: Playlist, quality: Int = 100) =
        invoke(listOf(playlist), 0..Int.MAX_VALUE, quality)[0]
}