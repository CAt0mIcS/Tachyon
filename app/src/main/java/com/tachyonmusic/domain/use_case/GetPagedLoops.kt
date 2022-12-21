package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.LoopRepository

class GetPagedLoops(
    private val loopRepository: LoopRepository
) {
    operator fun invoke(pageSize: Int) = loopRepository.getPagedLoops(pageSize)
}