package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.SongRepository

class ObserveSongs(
    private val repository: SongRepository
) {
    operator fun invoke() = repository.observe()
    operator fun invoke(mediaId: MediaId) = repository.observeByMediaId(mediaId)
}