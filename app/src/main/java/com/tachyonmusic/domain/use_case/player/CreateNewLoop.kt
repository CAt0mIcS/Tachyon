package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class CreateNewLoop(
    private val userRepo: UserRepository,
    private val browser: MediaBrowserController
) {
    suspend operator fun invoke(
        name: String
    ): Resource<Loop> {
        if (browser.playback?.mediaId == null || browser.timingData == null ||
            browser.timingData?.isEmpty() == true ||
            (browser.timingData!![0].startTime == 0L && browser.timingData!![0].endTime == browser.playback?.duration)
        )
            return Resource.Error(UiText.DynamicString("Invalid loop"))

        // TODO: Don't use Impl here

        val loop = RemoteLoopImpl.build(
            name,
            browser.playback!!.mediaId,
            TimingDataController(browser.timingData!!)
        )
        userRepo += loop
        userRepo.save()
        return Resource.Success(loop)
    }
}