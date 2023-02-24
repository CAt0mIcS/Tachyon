package com.tachyonmusic.domain.use_case

import com.tachyonmusic.presentation.util.SortOrder
import com.tachyonmusic.presentation.util.SortType
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.util.sortedBy
import kotlinx.coroutines.flow.map

class ObservePlaylists(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(
        sortType: SortType = SortType.AlphabeticalTitle,
        sortOrder: SortOrder = SortOrder.Ascending
    ) = playlistRepository.observe().map { it.sortedBy(sortType, sortOrder) }
}