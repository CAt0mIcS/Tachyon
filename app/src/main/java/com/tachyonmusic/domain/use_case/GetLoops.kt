package com.tachyonmusic.domain.use_case

import com.tachyonmusic.user.domain.UserRepository

class GetLoops(
    private val userRepository: UserRepository
) {
    operator fun invoke() = userRepository.loops
}