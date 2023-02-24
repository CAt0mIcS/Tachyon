package com.tachyonmusic.domain.use_case

import com.tachyonmusic.presentation.util.SortOrder
import com.tachyonmusic.presentation.util.SortType
import com.tachyonmusic.database.domain.repository.LoopRepository
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