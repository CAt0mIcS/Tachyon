package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateAndSaveNewCustomizedSong(
    private val songRepository: SongRepository,
    private val customizedSongRepository: CustomizedSongRepository,
    private val browser: MediaBrowserController
) {
    suspend operator fun invoke(
        name: String
    ) = withContext(Dispatchers.IO) {
        var isInvalid = false
        var playback: SinglePlayback? = null
        runOnUiThread {
            isInvalid = isInvalidPlayback() || hasNoTimingData() || isInvalidTimingData()
            playback = browser.currentPlayback.value
        }
        if (isInvalid)
            return@withContext Resource.Error(
                UiText.StringResource(
                    R.string.cannot_create_customizedSong,
                    name,
                    playback.toString(),
                    playback?.timingData.toString()
                )
            )


        /**
         * Building the new customizedSong by using either the [underlyingMediaId] of the current playback
         * which means that the current playback is a customizedSong or using the direct media id of the current
         * playback which means that it's a song TODO: Saving current playlist item as customizedSong
         */
        val songMediaId = playback!!.mediaId.underlyingMediaId ?: playback!!.mediaId
        val song = songRepository.findByMediaId(songMediaId) ?: return@withContext Resource.Error(
            UiText.StringResource(R.string.song_not_found, songMediaId.toString())
        )

        val customizedSong = CustomizedSongEntity(
            MediaId.ofLocalCustomizedSong(name, songMediaId),
            song.title,
            song.artist,
            song.duration,
            playback!!.timingData!!.timingData,
            currentTimingDataIndex = 0 // TODO
        )

        val res = customizedSongRepository.add(customizedSong)
        if (res is Resource.Error)
            return@withContext Resource.Error(res)

        Resource.Success(customizedSong)
    }

    private fun isInvalidPlayback() = browser.currentPlayback.value == null
    private fun hasNoTimingData() = browser.currentPlayback.value?.timingData.isNullOrEmpty()

    /**
     * Invalid if we only have one entry and it goes from the beginning to the end
     */
    private fun isInvalidTimingData() =
        browser.currentPlayback.value!!.timingData!!.timingData.all {
            it.startTime == 0.ms && it.endTime == browser.currentPlayback.value?.duration
        }

}