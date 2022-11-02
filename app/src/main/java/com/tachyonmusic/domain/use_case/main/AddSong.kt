package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.user.domain.UserRepository

class AddSong(
    private val userRepository: UserRepository
) {
    operator fun invoke(song: Song) {
        userRepository += song
    }
}