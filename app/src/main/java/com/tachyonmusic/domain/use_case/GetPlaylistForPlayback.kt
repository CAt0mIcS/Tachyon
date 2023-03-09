package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.playback_layers.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * OPTIMIZE: Only load artwork and isPlayable state if required
 * OPTIMIZE: Only rebuild entire playlist if some part of the items changes. E.g. if a loop is added.
 *  And not every time [invoke] is called
 */
class GetPlaylistForPlayback(
    private val settingsRepository: SettingsRepository,
    private val playbackRepository: PlaybackRepository
) {

    suspend operator fun invoke(
        playback: SinglePlayback?,
        sortParams: SortParameters = SortParameters()
    ) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext null

        when (playback) {
            is Song -> getSongPlaylist(playback, sortParams)
            is Loop -> getLoopPlaylist(playback, sortParams)
            else -> null
        }
    }


    private suspend fun getSongPlaylist(
        playback: SinglePlayback,
        sortParams: SortParameters
    ): Playlist {
        val settings = settingsRepository.getSettings()
        val items = (if (settings.combineDifferentPlaybackTypes)
            playbackRepository.getSongs() + playbackRepository.getLoops()
        else
            playbackRepository.getSongs()).sortedBy(sortParams)

        return RemotePlaylistImpl.build(
            MediaId.ofRemotePlaylist("com.tachyonmusic.SONGS:Combine:${settings.combineDifferentPlaybackTypes}"),
            items.toMutableList(),
            items.indexOf(playback)
        )
    }


    private suspend fun getLoopPlaylist(
        playback: SinglePlayback,
        sortParams: SortParameters
    ): Playlist {
        val settings = settingsRepository.getSettings()
        val items = (if (settings.combineDifferentPlaybackTypes)
            playbackRepository.getLoops() + playbackRepository.getSongs()
        else
            playbackRepository.getLoops()).sortedBy(sortParams)

        return RemotePlaylistImpl.build(
            MediaId.ofRemotePlaylist("com.tachyonmusic.LOOPS:Combine:${settings.combineDifferentPlaybackTypes}"),
            items.toMutableList(),
            items.indexOf(playback)
        )
    }
}