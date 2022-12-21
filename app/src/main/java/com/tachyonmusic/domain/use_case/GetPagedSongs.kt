package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.SongRepository

class GetPagedSongs(
    private val repository: SongRepository
) {
    operator fun invoke(pageSize: Int, prefetchDistance: Int = pageSize) =
        repository.getPagedSongs(pageSize, prefetchDistance)
}