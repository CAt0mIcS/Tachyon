package com.tachyonmusic.domain.use_case

import android.content.Context
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.util.isPlayable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


class GetSongs(
    private val repository: SongRepository,
    private val context: Context
) {
    suspend operator fun invoke(sortParams: SortParameters = SortParameters()) =
        withContext(Dispatchers.IO) {
            repository.getSongs().onEach { song ->
                song.isPlayable.update { song.mediaId.uri.isPlayable(context) }
            }.sortedBy(sortParams)
        }
}