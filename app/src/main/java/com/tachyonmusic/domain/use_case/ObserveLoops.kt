package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import kotlinx.coroutines.flow.map

class ObserveLoops(
    private val loopRepository: LoopRepository
) {
    operator fun invoke(
        sortParams: SortParameters = SortParameters()
    ) = loopRepository.observe().map { it.sortedBy(sortParams) }
}