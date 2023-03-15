package com.tachyonmusic.core.domain.playback

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface Song : SinglePlayback {
    override fun copy(): Song

    suspend fun loadArtworkAsync(
        resourceFlow: Flow<Resource<Artwork>>,
        onCompletion: suspend (MediaId?, Artwork?) -> Unit
    )
}
