package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.FileSongMetadataExtractor
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.ext.toBoolean
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.File

/**
 * Song stored in local storage with a path in the filesystem
 */
class LocalSongImpl(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Long
) : AbstractSong(mediaId, title, artist, duration) {

    override val playbackType = PlaybackType.Song.Local()

    val path: File
        get() = mediaId.path!!

    override val uri: Uri
        get() = Uri.fromFile(path.raw)

    constructor(parcel: Parcel) : this(
        MediaId(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong()
    ) {
        artwork.value = parcel.readParcelable(Artwork::class.java.classLoader)
        isArtworkLoading.value = parcel.readInt().toBoolean()
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalSongImpl> {
            override fun createFromParcel(parcel: Parcel) = LocalSongImpl(parcel)
            override fun newArray(size: Int): Array<LocalSongImpl?> = arrayOfNulls(size)
        }

        fun build(
            path: File,
            metadataExtractor: SongMetadataExtractor = FileSongMetadataExtractor()
        ): Song =
            metadataExtractor.loadMetadata(Uri.fromFile(path.raw)).run {
                if (this != null)
                    return@run LocalSongImpl(MediaId.ofLocalSong(path), title, artist, duration)
                else TODO("Invalid playback $path")
            }

        fun build(mediaId: MediaId) = build(mediaId.path!!)

        fun build(map: Map<String, Any?>) = build(MediaId(map["mediaId"]!! as String))
    }
}