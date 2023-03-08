package com.tachyonmusic.domain.use_case

import android.content.Context
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.util.setPlayableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GetSongs(
    private val repository: SongRepository,
    private val context: Context
) {
    suspend operator fun invoke(sortParams: SortParameters? = null) =
        withContext(Dispatchers.IO) {
            repository.getSongs().setPlayableState(context).apply {
                if (sortParams != null)
                    sortedBy(sortParams)
            }
        }
}