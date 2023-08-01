package com.tachyonmusic.media.data

import com.tachyonmusic.media.domain.SynchronizedState
import com.tachyonmusic.media.domain.model.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow

class SynchronizedStateImpl : SynchronizedState {
    override val playbackController = MutableStateFlow(PlaybackController.Local)
}