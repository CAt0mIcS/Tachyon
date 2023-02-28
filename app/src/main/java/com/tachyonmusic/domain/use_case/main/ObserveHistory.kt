package com.tachyonmusic.domain.use_case.main

import android.content.Context
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.util.isPlayable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ObserveHistory(
    private val historyRepository: HistoryRepository,
    private val context: Context
) {
    operator fun invoke() = historyRepository.observe().map {
        it.onEach { pb ->
            pb.isPlayable.update { pb.mediaId.uri.isPlayable(context) }
        }
    }
}