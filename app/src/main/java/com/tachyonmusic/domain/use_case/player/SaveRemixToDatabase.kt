package com.tachyonmusic.domain.use_case.player

import androidx.activity.ComponentActivity
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
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke(
        remix: RemixEntity,
        ignoreMaxRemixes: Boolean = false
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        if (ignoreMaxRemixes)
            remixRepository.add(remix)
        else {
            val maxRemixes = dataRepository.getData().maxRemixCount

            // TODO: Too slow?
            val numStoredRemixes = remixRepository.getRemixes().size
            if (numStoredRemixes >= maxRemixes) {
                Resource.Error(code = ERROR_NEEDS_TO_SHOW_AD)
            } else {
                remixRepository.add(remix)
            }

            /**
             * TODO
             *  Manage ad not loaded
             */
        }
    }


    companion object {
        const val ERROR_NEEDS_TO_SHOW_AD = 1
    }
}