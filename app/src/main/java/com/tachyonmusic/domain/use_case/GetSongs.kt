package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy


class GetSongs(
    private val repository: SongRepository
) {
    suspend operator fun invoke(
        sortParams: SortParameters = SortParameters()
    ) = repository.getSongs().onEach {
        it.isArtworkLoading.value = true
    }.sortedBy(sortParams)
}