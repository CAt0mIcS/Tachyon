package com.tachyonmusic.domain.use_case

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class PlayPlayback(
    private val browser: MediaBrowserController
) {
    operator fun invoke(playback: Playback?, playInPlaylist: Boolean = false): Resource<Unit> {
        // TODO: Validate playback and existence

        when (playback) {
            is SinglePlayback? -> {
                // is playing playlist
                if (playback != null && browser.associatedPlaylistState.value != null && playInPlaylist) {
                    try {
                        browser.seekTo(playback)
                    } catch (e: IllegalArgumentException) {
                        return Resource.Error(
                            UiText.StringResource(
                                R.string.playback_not_in_playlist,
                                playback.toString(),
                                browser.associatedPlaylistState.value.toString()
                            )
                        )
                    }
                } else
                    browser.playback = playback
            }
            is Playlist? -> browser.playPlaylist(playback)
            else -> TODO("Invalid playback type ${playback?.javaClass?.name.toString()}")
        }

        if (playback != null)
            browser.playWhenReady = true

        return Resource.Success()
    }
}