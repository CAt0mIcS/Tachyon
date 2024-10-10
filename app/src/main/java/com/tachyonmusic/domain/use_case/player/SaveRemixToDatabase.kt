package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext

class SaveRemixToDatabase(
    private val remixRepository: RemixRepository,
    private val dataRepository: DataRepository,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val mediaBrowser: MediaBrowserController,
    private val log: Logger
) {
    suspend operator fun invoke(
        remix: RemixEntity,
        playNewlyCreatedRemix: Boolean,
        ignoreMaxRemixes: Boolean = false,
        replaceExisting: Boolean = false
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        val storedRemixes = remixRepository.getRemixes()
        val maxRemixes = dataRepository.getData().maxRemixCount
        val remixAlreadyExists = storedRemixes.find { it.mediaId == remix.mediaId } != null

        if (!replaceExisting && remixAlreadyExists)
            Resource.Error(code = ERROR_REMIX_ALREADY_EXISTS)
        else if (!ignoreMaxRemixes && !remixAlreadyExists && storedRemixes.size >= maxRemixes) {
            Resource.Error(code = ERROR_NEEDS_TO_SHOW_AD)
        } else {
            val result = if (replaceExisting)
                remixRepository.addOrReplace(remix)
            else
                remixRepository.add(remix)

            result.apply {
                predefinedPlaylistsRepository.remixPlaylist.takeWhile { remixes ->
                    if (remixes.find { it.mediaId == remix.mediaId } != null) {
                        if (!playNewlyCreatedRemix) // TODO: Shouldn't be happening here
                            mediaBrowser.updatePredefinedPlaylist()
                        false
                    } else
                        true
                }.collect()
                log.info("Remix with name ${remix.mediaId.name} created successfully")
            }
        }

        /**
         * TODO
         *  Manage ad not loaded
         */
    }


    companion object {
        const val ERROR_NEEDS_TO_SHOW_AD = 1
        const val ERROR_REMIX_ALREADY_EXISTS = 1555
    }
}