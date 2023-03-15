package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.ext.toBoolean
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms

/**
 * Song stored in local storage with a path in the filesystem
 */
class LocalSongImpl(
    override val uri: Uri,
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Duration
) : AbstractSong(mediaId, title, artist, duration) {

    override val playbackType = PlaybackType.Song.Local()

    override fun copy(): Song = LocalSongImpl(uri, mediaId, title, artist, duration).let {
        it.artwork.value = artwork.value
        it.isArtworkLoading.value = isArtworkLoading.value
        it.isPlayable.value = isPlayable.value
        it
    }

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<Uri>(Uri::class.java.classLoader)!!,
        MediaId(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong().ms
    ) {
        artwork.value = parcel.readParcelable(Artwork::class.java.classLoader)
        isArtworkLoading.value = parcel.readInt().toBoolean()
        isPlayable.value = parcel.readInt().toBoolean()
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalSongImpl> {
            override fun createFromParcel(parcel: Parcel) = LocalSongImpl(parcel)
            override fun newArray(size: Int): Array<LocalSongImpl?> = arrayOfNulls(size)
        }
    }
}