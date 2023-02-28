package com.tachyonmusic.domain.use_case

import android.content.Context
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.util.isPlayable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class GetHistory(
    private val historyRepository: HistoryRepository,
    private val context: Context
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        historyRepository.getHistory().onEach { pb ->
            pb.isPlayable.update { pb.mediaId.uri.isPlayable(context) }
        }
    }
}