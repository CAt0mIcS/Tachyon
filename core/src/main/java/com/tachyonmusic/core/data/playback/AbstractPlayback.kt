package com.tachyonmusic.core.data.playback

import com.tachyonmusic.core.domain.playback.Playback

abstract class AbstractPlayback : Playback {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }

    override fun toString() = mediaId.toString()

    override fun describeContents() = 0
}