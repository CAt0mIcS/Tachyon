package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.domain.AudioEffectController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateAndSaveNewCustomizedSong(
    private val songRepository: SongRepository,
    private val customizedSongRepository: CustomizedSongRepository,
    private val browser: MediaBrowserController,
    private val audioEffectController: AudioEffectController
) {
    suspend operator fun invoke(
        name: String
    ) = withContext(Dispatchers.IO) {
        var isInvalid = false
        var playback: SinglePlayback? = null
        runOnUiThread {
            isInvalid = isInvalidPlayback()
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
         * playback which means that it's a song TODO: Saving current playlist item as CustomizedSong
         */
        val songMediaId = playback!!.mediaId.underlyingMediaId ?: playback!!.mediaId
        val song = songRepository.findByMediaId(songMediaId) ?: return@withContext Resource.Error(
            UiText.StringResource(R.string.song_not_found, songMediaId.toString())
        )

        val customizedSong = runOnUiThread {
            CustomizedSongEntity(
                MediaId.ofLocalCustomizedSong(name, songMediaId),
                song.title,
                song.artist,
                song.duration,
                playback!!.timingData?.timingData,
                currentTimingDataIndex = 0, // TODO
                audioEffectController.bass,
                audioEffectController.virtualizerStrength,
                audioEffectController.bands,
                audioEffectController.playbackParams,
                audioEffectController.reverb
            )
        }

        val res = customizedSongRepository.add(customizedSong)
        if (res is Resource.Error)
            return@withContext Resource.Error(res)

        Resource.Success(customizedSong)
    }

    private fun isInvalidPlayback() = browser.currentPlayback.value == null
}