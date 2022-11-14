package com.tachyonmusic.domain.use_case

import com.tachyonmusic.user.domain.UserRepository

class GetSongs(
    private val userRepository: UserRepository
) {
    operator fun invoke() = userRepository.songs
}