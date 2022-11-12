package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.user.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddNewPlaybackToHistory(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(playback: Playback?) = withContext(Dispatchers.IO) {
        if (playback != null) {
            userRepository.addHistory(playback)
            userRepository.save()
        }

    }
}