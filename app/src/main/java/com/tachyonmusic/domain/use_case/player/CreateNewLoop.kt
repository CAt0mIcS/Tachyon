package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.data.playback.RemoteLoop
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.user.domain.UserRepository

class CreateNewLoop(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        songMediaId: MediaId,
        startTime: Long,
        endTime: Long
    ) {
        repository += RemoteLoop.build(
            name,
            songMediaId,
            TimingDataController(listOf(TimingData(startTime, endTime)))
        )
    }
}