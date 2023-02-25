package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.media.core.SortOrder
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.SortType
import com.tachyonmusic.media.core.sortedBy
import kotlinx.coroutines.flow.map

class ObservePlaylists(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(
        sortParams: SortParameters = SortParameters()
    ) = playlistRepository.observe().map { it.sortedBy(sortParams) }
}