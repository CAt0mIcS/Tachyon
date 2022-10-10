package com.tachyonmusic.presentation.player.component.cast

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import com.tachyonmusic.app.R

class MediaRouteViewModel(context: Context) : ViewModel() {
    private val mediaRouteActionProvider = MediaRouteActionProvider(context)

    val buttonView: View = mediaRouteActionProvider.onCreateActionView()

    init {
        mediaRouteActionProvider.routeSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()
    }


    fun onClick() {
        mediaRouteActionProvider.onPerformDefaultAction()
    }
}