package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class CreateAndSaveNewLoop(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val browser: MediaBrowserController
) {
    // TODO: [UiText.StringResource] instead of [UiText.DynamicString]
    suspend operator fun invoke(
        name: String
    ): Resource<Unit> {
        if (browser.playback?.mediaId == null || browser.timingData == null ||
            browser.timingData?.isEmpty() == true ||
            (browser.timingData!![0].startTime == 0L && browser.timingData!![0].endTime == browser.playback?.duration)
        )
            return Resource.Error(UiText.DynamicString("Invalid loop"))

        /**
         * Building the new loop by using either the [underlyingMediaId] of the current playback
         * which means that the current playback is a loop or using the direct media id of the current
         * playback which means that it's a song TODO: Saving current playlist item as loop
         */
        val songMediaId = browser.playback!!.mediaId.underlyingMediaId ?: browser.playback!!.mediaId
        val song = songRepository.findByMediaId(songMediaId) ?: return Resource.Error(
            UiText.DynamicString("Unknown song $songMediaId")
        )

        val loop = LoopEntity(
            MediaId.ofRemoteLoop(name, songMediaId),
            song.title,
            song.artist,
            song.duration,
            browser.timingData!!,
            currentTimingDataIndex = 0, // TODO
            song.artworkType,
            song.artworkUrl
        )

        loopRepository.add(loop)
        return Resource.Success()
    }
}