package com.tachyonmusic.domain.use_case

import com.tachyonmusic.user.domain.UserRepository

class GetPlaylists(
    private val userRepository: UserRepository
) {
    operator fun invoke() = userRepository.playlists
}