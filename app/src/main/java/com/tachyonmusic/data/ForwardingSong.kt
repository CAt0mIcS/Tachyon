package com.tachyonmusic.data

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Song

class ForwardingSong(
    private val song: Song
) : Song {
    val listener: Listener? = null

    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Long
        get() = song.duration
    override val artwork: Bitmap?
        get() = song.artwork

    override fun unloadArtwork() {
        listener?.onUnloadArtwork(song)
    }

    override var timingData: TimingDataController
        get() = song.timingData
        set(value) {
            listener?.onSetTimingData(song, value)
        }
    override val uri: Uri
        get() = song.uri
    override val mediaId: MediaId
        get() = song.mediaId
    override val playbackType: PlaybackType
        get() = song.playbackType

    override fun toMediaItem() = song.toMediaItem()

    override fun toMediaMetadata() = song.toMediaMetadata()

    override fun toHashMap() = song.toHashMap()

    override suspend fun loadBitmap(onDone: suspend () -> Unit) {
        listener?.onLoadBitmap(song, onDone)
    }

    override fun equals(other: Any?) = song == other

    override fun toString() = song.toString()

    override fun describeContents() = song.describeContents()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<ForwardingSong> {
        override fun createFromParcel(parcel: Parcel): ForwardingSong {
            TODO("Not yet implemented")
        }

        override fun newArray(size: Int): Array<ForwardingSong?> {
            return arrayOfNulls(size)
        }
    }

    interface Listener {
        fun onUnloadArtwork(song: Song) {
            song.unloadArtwork()
        }

        suspend fun onLoadBitmap(song: Song, onDone: suspend () -> Unit) {
            song.loadBitmap(onDone)
        }

        fun onSetTimingData(song: Song, timingData: TimingDataController) {
            song.timingData = timingData
        }
    }
}