package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GetSongs(
    private val repository: SongRepository
) {
    suspend operator fun invoke(sortParams: SortParameters = SortParameters()) =
        withContext(Dispatchers.IO) {
            repository.getSongs().sortedBy(sortParams)
        }
}