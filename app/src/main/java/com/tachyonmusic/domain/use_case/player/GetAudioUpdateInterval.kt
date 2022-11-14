package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.user.domain.UserRepository
import kotlin.time.Duration.Companion.milliseconds

class GetAudioUpdateInterval(private val userRepository: UserRepository) {
    operator fun invoke() = 100.milliseconds
}