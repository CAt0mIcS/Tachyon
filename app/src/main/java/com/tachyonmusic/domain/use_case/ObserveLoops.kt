package com.tachyonmusic.domain.use_case

import android.content.Context
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.media.util.isPlayable
import com.tachyonmusic.util.setPlayableState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ObserveLoops(
    private val loopRepository: LoopRepository,
    private val context: Context
) {
    operator fun invoke(
        sortParams: SortParameters = SortParameters()
    ) = loopRepository.observe().map {
        it.setPlayableState(context).sortedBy(sortParams)
    }
}