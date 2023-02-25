package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.media.core.SortOrder
import com.tachyonmusic.media.core.SortType
import com.tachyonmusic.util.sortedBy
import kotlinx.coroutines.flow.map

class ObserveLoops(
    private val loopRepository: LoopRepository
) {
    operator fun invoke(
        sortType: SortType = SortType.AlphabeticalTitle,
        sortOrder: SortOrder = SortOrder.Ascending
    ) = loopRepository.observe().map { it.sortedBy(sortType, sortOrder) }
}