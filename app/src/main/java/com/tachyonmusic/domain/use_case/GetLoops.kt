package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.LoopRepository

class GetLoops(
    private val loopRepository: LoopRepository
) {
    suspend operator fun invoke() = loopRepository.getLoops()
}