package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.LoadArtwork
import com.tachyonmusic.database.util.ArtworkUpdateInfo
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateArtworks(
    private val songRepo: SongRepository,
    private val isFirstAppStart: IsFirstAppStart,
    private val loadArtwork: LoadArtwork,
    private val log: Logger = Log()
) {
    suspend operator fun invoke(shouldUpdate: Boolean = isFirstAppStart()) =
        withContext(Dispatchers.IO) {
            if (shouldUpdate) {
                log.debug("Loading song artworks")

                val infos = mutableListOf<ArtworkUpdateInfo?>()
                songRepo.getSongsWithArtworkTypes(ArtworkType.NO_ARTWORK).forEach { song ->
                    infos += loadArtwork(song)
                    if (infos.lastOrNull() != null)
                        updateArtwork(infos.lastOrNull()!!)
                }

//                infos.forEach {
//                    if (it != null)
//                        updateArtwork(it)
//                }
            }
        }
}