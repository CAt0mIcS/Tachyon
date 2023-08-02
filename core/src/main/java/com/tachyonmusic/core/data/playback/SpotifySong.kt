package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.ext.toBoolean
import com.tachyonmusic.core.data.ext.toInt
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms

class SpotifySong(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Duration,
    override val isHidden: Boolean
) : AbstractSong(mediaId, title, artist, duration) {
    override val playbackType = PlaybackType.Song.Spotify()

    override fun copy(): Song = SpotifySong(mediaId, title, artist, duration, isHidden).let {
        it.artwork = artwork
        it.isArtworkLoading = isArtworkLoading
        it.isPlayable = isPlayable
        it.timingData = timingData?.copy()
        it
    }

    override val uri: Uri = mediaId.uri ?: TODO("Invalid Spotify mediaId: $mediaId")

    constructor(parcel: Parcel) : this(
        MediaId(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong().ms,
        parcel.readInt().toBoolean()
    ) {
        artwork = parcel.readParcelable(Artwork::class.java.classLoader)
        isArtworkLoading = parcel.readInt().toBoolean()
        isPlayable = parcel.readInt().toBoolean()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mediaId.source)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration.inWholeMilliseconds)
        parcel.writeInt(isHidden.toInt())
        parcel.writeParcelable(artwork, flags)
        parcel.writeInt(isArtworkLoading.toInt())
        parcel.writeInt(isPlayable.toInt())
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SpotifySong> {
            override fun createFromParcel(parcel: Parcel) = SpotifySong(parcel)
            override fun newArray(size: Int): Array<SpotifySong?> = arrayOfNulls(size)
        }
    }
}