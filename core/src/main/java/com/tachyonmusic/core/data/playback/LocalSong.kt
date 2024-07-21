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

/**
 * Song stored in local storage with a path in the filesystem
 */
class LocalSong(
    override val uri: Uri,
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Duration,
    override val isHidden: Boolean,
    override val timestampCreatedAddedEdited: Long
) : AbstractSong(mediaId, title, artist, duration) {

    override val playbackType = PlaybackType.Song.Local()

    override fun copy(): Song = LocalSong(
        uri,
        mediaId,
        title,
        artist,
        duration,
        isHidden,
        timestampCreatedAddedEdited
    ).let {
        it.artwork = artwork
        it.isArtworkLoading = isArtworkLoading
        it.isPlayable = isPlayable
        it.timingData = timingData?.copy()
        it.album = album
        it
    }

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<Uri>(Uri::class.java.classLoader)!!,
        MediaId(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong().ms,
        parcel.readInt().toBoolean(),
        parcel.readLong()
    ) {
        artwork = parcel.readParcelable(Artwork::class.java.classLoader)
        isArtworkLoading = parcel.readInt().toBoolean()
        isPlayable = parcel.readInt().toBoolean()
        album = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(mediaId.source)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration.inWholeMilliseconds)
        parcel.writeInt(isHidden.toInt())
        parcel.writeLong(timestampCreatedAddedEdited)
        parcel.writeParcelable(artwork, flags)
        parcel.writeInt(isArtworkLoading.toInt())
        parcel.writeInt(isPlayable.toInt())
        parcel.writeString(album)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalSong> {
            override fun createFromParcel(parcel: Parcel) = LocalSong(parcel)
            override fun newArray(size: Int): Array<LocalSong?> = arrayOfNulls(size)
        }
    }
}