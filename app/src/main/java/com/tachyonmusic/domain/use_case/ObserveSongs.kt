package com.tachyonmusic.domain.use_case

import android.content.Context
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.media.util.isPlayable
import com.tachyonmusic.util.setPlayableState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ObserveSongs(
    private val repository: SongRepository,
    private val context: Context
) {
    operator fun invoke(
        sortParams: SortParameters = SortParameters()
    ) = repository.observe().map {
        it.setPlayableState(context).sortedBy(sortParams)
    }

    operator fun invoke(mediaId: MediaId) = repository.observeByMediaId(mediaId)
}