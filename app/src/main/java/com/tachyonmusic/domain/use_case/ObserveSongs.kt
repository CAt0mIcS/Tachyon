package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.core.SortOrder
import com.tachyonmusic.media.core.SortType
import com.tachyonmusic.util.sortedBy
import kotlinx.coroutines.flow.map

class ObserveSongs(
    private val repository: SongRepository
) {
    operator fun invoke(
        sortType: SortType = SortType.AlphabeticalTitle,
        sortOrder: SortOrder = SortOrder.Ascending
    ) = repository.observe().map { it.sortedBy(sortType, sortOrder) }

    operator fun invoke(mediaId: MediaId) = repository.observeByMediaId(mediaId)
}