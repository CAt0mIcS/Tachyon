package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.presentation.util.SortOrder
import com.tachyonmusic.presentation.util.SortType
import com.tachyonmusic.util.sortedBy

class GetSongs(
    private val repository: SongRepository
) {
    suspend operator fun invoke(
        sortType: SortType = SortType.AlphabeticalTitle,
        sortOrder: SortOrder = SortOrder.Ascending
    ) = repository.getSongs().onEach {
        it.isArtworkLoading.value = true
    }.sortedBy(sortType, sortOrder)
}