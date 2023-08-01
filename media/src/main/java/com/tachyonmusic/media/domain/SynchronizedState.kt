package com.tachyonmusic.media.domain

import com.tachyonmusic.media.domain.model.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow

// TODO: Not really great design here...
interface SynchronizedState {
    val playbackController: MutableStateFlow<PlaybackController>
}