package com.tachyonmusic.media.domain.use_case

import android.content.Context
import androidx.media3.common.MediaItem
import com.tachyonmusic.core.domain.playback.*
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toLoop
import com.tachyonmusic.database.util.toSong
import com.tachyonmusic.media.R
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.media.util.isPlayable
import com.tachyonmusic.media.util.setArtworkFromResource
import com.tachyonmusic.media.util.toMediaItems
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class GetPlaylistForPlayback(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val settingsRepository: SettingsRepository,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val context: Context
) {
    data class ActivePlaylist(
        val mediaItems: List<MediaItem>,
        val playbackItems: List<SinglePlayback>,
        val initialWindowIndex: Int
    )

    suspend operator fun invoke(
        playback: Playback?,
        sortParams: SortParameters,
    ): Resource<ActivePlaylist> = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext Resource.Error(UiText.StringResource(R.string.invalid_playback))

        var initialWindowIndex: Int? = null
        var mediaItems: List<MediaItem>? = null
        var playbackItems: List<SinglePlayback>? = null
        val combinePlaybackTypes = settingsRepository.getSettings().combineDifferentPlaybackTypes

        when (playback) {
            is Song -> {
                val songEntities = songEntities(sortParams)
                val playbacks = if (combinePlaybackTypes)
                    songEntities.map { it.toSong() } + loops(sortParams)
                else
                    songEntities.map { it.toSong() }

                getOrLoadArtwork(songEntities).onEach { res ->
                    playbacks.setArtworkFromResource(res)
                }.collect()

                initialWindowIndex = playbacks.indexOf(playback)
                mediaItems = playbacks.map { it.toMediaItem() }
                playbackItems = playbacks
            }

            is Loop -> {
                val loopEntities = loopEntities(sortParams)
                val songEntities = songEntities(sortParams)
                val playbacks = if (combinePlaybackTypes)
                    loopEntities.map { it.toLoop() } + songEntities.map { it.toSong() }
                else
                    loopEntities.map { it.toLoop() }

                val songsOfLoops = loopEntities.map { loop ->
                    songEntities.find { loop.mediaId.underlyingMediaId == it.mediaId }!!
                } + if (combinePlaybackTypes) songEntities else emptyList()

                getOrLoadArtwork(songsOfLoops).onEach { res ->
                    playbacks.setArtworkFromResource(res)
                }.collect()

                initialWindowIndex = playbacks.indexOf(playback)
                mediaItems = playbacks.map { it.toMediaItem() }
                playbackItems = playbacks
            }

            is Playlist -> {
                initialWindowIndex = playback.currentPlaylistIndex

                // TODO: What if it.underlying song is null? Warning? Error?
                val validPlaybacks = playback.playbacks.filter {
                    it.mediaId.uri.isPlayable(context)
                }

                getOrLoadArtwork(validPlaybacks.map { it.underlyingSong }).onEach { res ->
                    validPlaybacks.setArtworkFromResource(res)
                }.collect()

                mediaItems = validPlaybacks.toMediaItems()
                playbackItems = validPlaybacks
            }
        }

        if (mediaItems == null || initialWindowIndex == null || playbackItems == null)
            return@withContext Resource.Error(UiText.StringResource(R.string.invalid_arguments))

        Resource.Success(ActivePlaylist(mediaItems, playbackItems, initialWindowIndex))
    }


    private suspend fun songEntities(sortParams: SortParameters) =
        songRepository.getSongEntities().filter {
            it.mediaId.uri.isPlayable(context)
        }.sortedBy(sortParams)


    private suspend fun loopEntities(sortParams: SortParameters) =
        loopRepository.getLoopEntities().filter {
            it.mediaId.uri.isPlayable(context)
        }.sortedBy(sortParams)

    private suspend fun loops(sortParams: SortParameters) = loopRepository.getLoops().filter {
        it.mediaId.uri.isPlayable(context)
    }.sortedBy(sortParams)
}