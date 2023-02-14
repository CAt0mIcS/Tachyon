package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.runOnUiThread
import com.tachyonmusic.util.runOnUiThreadAsync

class CreateAndSaveNewLoop(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val browser: MediaBrowserController
) {
    // TODO: [UiText.StringResource] instead of [UiText.DynamicString]
    suspend operator fun invoke(
        name: String
    ): Resource<LoopEntity> {
        var isInvalid = false
        var playback: Playback? = null
        var timingData: TimingDataController? = null
        runOnUiThread {
            isInvalid = isInvalidPlayback() || hasNoTimingData() || isInvalidTimingData()
            playback = browser.playback
            timingData = browser.timingData
        }
        if (isInvalid)
            return Resource.Error(UiText.DynamicString("Invalid loop"))


        /**
         * Building the new loop by using either the [underlyingMediaId] of the current playback
         * which means that the current playback is a loop or using the direct media id of the current
         * playback which means that it's a song TODO: Saving current playlist item as loop
         */
        val songMediaId = playback!!.mediaId.underlyingMediaId ?: playback!!.mediaId
        val song = songRepository.findByMediaId(songMediaId) ?: return Resource.Error(
            UiText.DynamicString("Unknown song $songMediaId")
        )

        val loop = LoopEntity(
            MediaId.ofRemoteLoop(name, songMediaId),
            song.title,
            song.artist,
            song.duration,
            timingData!!.timingData,
            currentTimingDataIndex = 0 // TODO
        )

        loopRepository.add(loop)
        return Resource.Success(loop)
    }

    private fun isInvalidPlayback() = browser.playback == null
    private fun hasNoTimingData() = browser.timingData?.timingData.isNullOrEmpty()

    /**
     * Invalid if we only have one entry and it goes from the beginning to the end
     */
    private fun isInvalidTimingData() = browser.timingData!!.timingData.all {
        it.startTime == 0L && it.endTime == browser.playback?.duration
    }

}