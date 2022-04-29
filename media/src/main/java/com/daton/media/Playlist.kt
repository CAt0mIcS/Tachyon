package com.daton.media

import android.support.v4.media.MediaMetadataCompat
import com.daton.media.ext.toMediaItem
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player


class Playlist(var player: Player) :
    ArrayList<MediaMetadataCompat>() {

    var currentPlaybackIndex: Int = 0


    fun play(items: List<MediaMetadataCompat>, itemToPlay: MediaMetadataCompat? = null) {
        addAll(items)

        player.setMediaItems(items.map { it.toMediaItem() })

        val initialWindowIndex = if (itemToPlay == null) 0 else indexOf(itemToPlay)
        // TODO: [itemToPlay] not in [playlist]
        player.seekTo(initialWindowIndex, C.TIME_UNSET)
    }
}

