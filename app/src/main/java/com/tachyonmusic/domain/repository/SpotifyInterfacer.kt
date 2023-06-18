package com.tachyonmusic.domain.repository

import android.app.Activity
import android.content.Intent
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.StateFlow

interface SpotifyInterfacer {
    fun authorize(activity: Activity)
    fun onAuthorization(requestCode: Int, resultCode: Int, intent: Intent?)

    val currentPosition: Duration?

    val currentPlayback: StateFlow<SpotifySong?>
    val isPlaying: StateFlow<Boolean>
    val repeatMode: StateFlow<RepeatMode>

    /**
     * @param uri either the uri of a playlist or track
     * @param index index to play if [uri] is a playlist uri
     */
    fun play(uri: String, index: Int? = null)
    fun resume()
    fun pause()
    fun seekTo(pos: Duration)
    fun seekTo(playlistUri: String, index: Int, pos: Duration)

    fun setRepeatMode(repeatMode: RepeatMode)
}