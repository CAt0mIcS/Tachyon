package com.tachyonmusic.media.util

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.use_case.UpdateInfo
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.update


fun Playback.setArtworkFromResource(res: Resource<UpdateInfo>) {
    when (res) {
        is Resource.Loading -> isArtworkLoading.update { true }
        else -> {
            artwork.update { res.data!!.artwork }
            isArtworkLoading.update { false }
        }
    }
}

fun List<Playback>.setArtworkFromResource(res: Resource<UpdateInfo>) {
    this[res.data!!.i].setArtworkFromResource(res)
}