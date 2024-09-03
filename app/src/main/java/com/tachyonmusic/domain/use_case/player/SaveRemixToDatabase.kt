package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.predefinedRemixPlaylistMediaId
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext

class SaveRemixToDatabase(
    private val remixRepository: RemixRepository,
    private val dataRepository: DataRepository,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val mediaBrowser: MediaBrowserController
) {
    suspend operator fun invoke(
        remix: RemixEntity,
        playNewlyCreatedRemix: Boolean,
        ignoreMaxRemixes: Boolean = false
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        val maxRemixes = dataRepository.getData().maxRemixCount

        // TODO: Too slow?
        val numStoredRemixes = remixRepository.getRemixes().size
        if (numStoredRemixes >= maxRemixes && !ignoreMaxRemixes) {
            Resource.Error(code = ERROR_NEEDS_TO_SHOW_AD)
        } else {
            remixRepository.add(remix).apply {
                predefinedPlaylistsRepository.remixPlaylist.takeWhile { remixes ->
                    if (remixes.find { it.mediaId == remix.mediaId } != null) {
                        if (!playNewlyCreatedRemix) // TODO: Shouldn't be happening here
                            mediaBrowser.updatePredefinedPlaylist()
                        false
                    } else
                        true
                }.collect()
            }
        }

        /**
         * TODO
         *  Manage ad not loaded
         */
    }


    companion object {
        const val ERROR_NEEDS_TO_SHOW_AD = 1
    }
}