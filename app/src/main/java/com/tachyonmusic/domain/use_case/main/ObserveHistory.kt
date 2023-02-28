package com.tachyonmusic.domain.use_case.main

import android.content.Context
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.media.util.isPlayable
import com.tachyonmusic.util.setPlayableState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ObserveHistory(
    private val historyRepository: HistoryRepository,
    private val context: Context
) {
    operator fun invoke() = historyRepository.observe().map { it.setPlayableState(context) }
}