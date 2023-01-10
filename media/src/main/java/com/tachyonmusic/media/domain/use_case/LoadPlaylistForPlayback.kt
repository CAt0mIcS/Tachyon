package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.MediaItem
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toSong
import com.tachyonmusic.media.R
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

class LoadPlaylistForPlayback(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val settingsRepository: SettingsRepository,
    private val getOrLoadArtwork: GetOrLoadArtwork
) {
    suspend operator fun invoke(playback: Playback?): Resource<Pair<List<MediaItem>, Int>> {
        if (playback == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_playback))

        var initialWindowIndex: Int? = null
        var items: List<MediaItem>? = null
        val combinePlaybackTypes = settingsRepository.getSettings().combineDifferentPlaybackTypes

        when (playback) {
            is Song -> {
                val songEntities = songRepository.getSongEntities()
                val playbacks = if (combinePlaybackTypes)
                    songEntities.map { it.toSong() } + loopRepository.getLoops()
                else
                    songEntities.map { it.toSong() }

                getOrLoadArtwork(songEntities).onEach {
                    if (it is Resource.Success)
                        playbacks[it.data!!.i].artwork.value = it.data!!.artwork
                }.collect()

                initialWindowIndex = playbacks.indexOf(playback)
                items = playbacks.map { it.toMediaItem() }
            }

            is Loop -> {
                TODO()
            }

            is Playlist -> {
                TODO()
            }
        }

        if (items == null || initialWindowIndex == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

        return Resource.Success(items to initialWindowIndex)
    }
}