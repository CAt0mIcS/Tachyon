package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.flow

class LoadAlbumArt {
    operator fun invoke(items: List<Song>) = flow {
        for (item in items) {
            emit(Resource.Loading(item))
            item.loadBitmap {
                if (item.artwork != null) {
                    emit(Resource.Success(item))
                } else
                    emit(
                        Resource.Error(
                            UiText.StringResource(
                                R.string.no_album_art,
                                item.toString()
                            ), item
                        )
                    )
            }
        }
    }
}