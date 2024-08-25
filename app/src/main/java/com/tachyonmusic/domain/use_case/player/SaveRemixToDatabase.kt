package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.domain.repository.AdInterface
import com.tachyonmusic.util.Config
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveRemixToDatabase(
    private val remixRepository: RemixRepository,
    private val dataRepository: DataRepository,
    private val adInterface: AdInterface
) {
    suspend operator fun invoke(remix: RemixEntity): Resource<Unit> = withContext(Dispatchers.IO) {
        val maxRemixes = dataRepository.getData().maxRemixCount

        // TODO: Too slow?
        val numStoredRemixes = remixRepository.getRemixes().size
        if (numStoredRemixes >= maxRemixes) {
            adInterface.showRewardAdSuspend {
                withContext(Dispatchers.IO) {
                    dataRepository.update(maxRemixCount = numStoredRemixes + Config.MAX_REMIX_INCREMENT_AMOUNT)
                    remixRepository.add(remix)
                }
            }
        } else {
            remixRepository.add(remix)
        }

        /**
         * TODO
         *  Manage ad not loaded
         *  manage saving error (return it here)
         */
        Resource.Success()
    }
}