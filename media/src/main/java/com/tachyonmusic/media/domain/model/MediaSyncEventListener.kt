package com.tachyonmusic.media.domain.model

import com.tachyonmusic.core.domain.playback.SinglePlayback

interface MediaSyncEventListener {
    fun onConnected() {}
    fun onAudioSessionIdChanged(audioSessionId: Int) {}

    fun onControlDispatched(controller: PlaybackController) {}
    fun onMediaItemTransition(playback: SinglePlayback?, source: PlaybackController) {}
}