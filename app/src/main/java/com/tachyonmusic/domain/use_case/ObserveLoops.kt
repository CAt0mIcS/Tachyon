package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.LoopRepository

class ObserveLoops(
    private val loopRepository: LoopRepository
) {
    operator fun invoke() = loopRepository.observe()
}