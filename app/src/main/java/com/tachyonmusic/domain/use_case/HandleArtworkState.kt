package com.tachyonmusic.domain.use_case

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class HandleArtworkState(
    browser: MediaBrowserController,
    private val application: Application
) : MediaStateHandler(browser) {
    private val _artwork = mutableStateOf<Artwork?>(null)
    val artwork: State<Artwork?> = _artwork

    override fun onPlaybackTransition(playback: Playback?) {
        if (playback !is SinglePlayback) {
            _artwork.value = null
            return
        }

//        launch(Dispatchers.IO) {
//            // TODO: Shouldn't use hard-coded size
//            // TODO: Better way of updating state here? (StateFlow in [SinglePlayback.artwork], State in [HandleArtworkState]
//            playback.loadArtwork(1000).map {
//                when (it) {
//                    is Resource.Error -> Log.e(
//                        "PlayerViewModel",
//                        "${playback.title} - ${playback.artist}: ${it.message!!.asString(application)}"
//                    )
//                    is Resource.Success -> {
//                        Log.d(
//                            "PlayerViewModel",
//                            "Successfully loaded cover art for ${playback.title} - ${playback.artist}"
//                        )
//                    }
//                    else -> {}
//                }
//            }.collect()
//            _artwork.value = playback.artwork.value
//        }
    }
}
