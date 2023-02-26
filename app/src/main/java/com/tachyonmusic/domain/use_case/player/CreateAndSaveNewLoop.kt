package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateAndSaveNewLoop(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val browser: MediaBrowserController
) {
    suspend operator fun invoke(
        name: String
    ) = withContext(Dispatchers.IO) {
        var isInvalid = false
        var playback: Playback? = null
        var timingData: TimingDataController? = null
        runOnUiThread {
            isInvalid = isInvalidPlayback() || hasNoTimingData() || isInvalidTimingData()
            playback = browser.playback
            timingData = browser.timingData
        }
        if (isInvalid)
            return@withContext Resource.Error(
                UiText.StringResource(
                    R.string.cannot_create_loop,
                    name,
                    playback.toString(),
                    timingData.toString()
                )
            )


        /**
         * Building the new loop by using either the [underlyingMediaId] of the current playback
         * which means that the current playback is a loop or using the direct media id of the current
         * playback which means that it's a song TODO: Saving current playlist item as loop
         */
        val songMediaId = playback!!.mediaId.underlyingMediaId ?: playback!!.mediaId
        val song = songRepository.findByMediaId(songMediaId) ?: return@withContext Resource.Error(
            UiText.StringResource(R.string.song_not_found, songMediaId.toString())
        )

        val loop = LoopEntity(
            MediaId.ofRemoteLoop(name, songMediaId),
            song.title,
            song.artist,
            song.duration,
            timingData!!.timingData,
            currentTimingDataIndex = 0 // TODO
        )

        val res = loopRepository.add(loop)
        if (res is Resource.Error)
            return@withContext Resource.Error(res)

        Resource.Success(loop)
    }

    private fun isInvalidPlayback() = browser.playback == null
    private fun hasNoTimingData() = browser.timingData?.timingData.isNullOrEmpty()

    /**
     * Invalid if we only have one entry and it goes from the beginning to the end
     */
    private fun isInvalidTimingData() = browser.timingData!!.timingData.all {
        it.startTime == 0.ms && it.endTime == browser.playback?.duration
    }

}