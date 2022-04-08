package com.example.mucify.service

import android.os.Bundle
import com.example.mucify.device.BrowserTree
import com.example.mucify.device.MediaSource
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSource: MediaSource

    private val browserTree: BrowserTree by lazy {
        BrowserTree(mediaSource)
    }

    override fun onCreate() {
        super.onCreate()

        /**
         * Starts asynchronously loading all possible media playbacks
         */
        mediaSource = MediaSource(this)
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        /**
         * We'll for now allow everyone to connect
         */
        return BrowserRoot(BrowserTree.ROOT, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaItem>>
    ) {
        val resultsSent = mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                val children = browserTree[parentId]?.map { item ->
                    MediaItem(item.description, 0)
                }
                result.sendResult(children)
            } else {
                TODO("Handle error that MediaSource wasn't initialized properly")
            }
        }

        // If the results are not ready, the service must "detach" the results before
        // the method returns. After the source is ready, the lambda above will run,
        // and the caller will be notified that the results are ready.
        if (!resultsSent)
            result.detach()
    }
}
