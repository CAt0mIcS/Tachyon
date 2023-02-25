package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import kotlinx.coroutines.flow.map

class ObserveSongs(
    private val repository: SongRepository
) {
    operator fun invoke(
        sortParams: SortParameters = SortParameters()
    ) = repository.observe().map { it.sortedBy(sortParams) }

    operator fun invoke(mediaId: MediaId) = repository.observeByMediaId(mediaId)
}