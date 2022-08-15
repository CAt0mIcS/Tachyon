package com.daton.media.device

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MetadataKeys
import com.daton.media.ext.albumArt
import com.daton.media.ext.artist
import com.daton.media.ext.mediaId
import com.daton.media.ext.title
import com.google.android.exoplayer2.MediaItem
import java.io.File

data class Playlist(
    val name: String,
    val playbacks: MutableList<SinglePlayback> = mutableListOf(),
    var currentPlaylistIndex: Int = 0
) : Playback() {

    override val mediaId: String
        get() = "*playlist*$name"

    override val path: File?
        get() = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelableArray(SinglePlayback::class.java.classLoader)!! as MutableList<SinglePlayback>,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelableArray(playbacks as Array<Parcelable>, flags)
        parcel.writeInt(currentPlaylistIndex)
    }

    override fun toMediaMetadata(): MediaMetadataCompat {
        if (currentPlaylistIndex != -1)
            return playbacks[currentPlaylistIndex].toMediaMetadata()
        return MediaMetadataCompat.Builder().also { metadata -> metadata.mediaId = mediaId }.build()
    }

    override fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem =
        MediaBrowserCompat.MediaItem(toMediaDescriptionCompat(), 0)

    override fun toMediaDescriptionCompat(): MediaDescriptionCompat =
        MediaDescriptionCompat.Builder().also { desc ->
            desc.setMediaId(mediaId)
            desc.setExtras(Bundle().apply { putParcelable(MetadataKeys.Playback, this@Playlist) })
        }.build()

    fun toMediaBrowserMediaItemList(): List<MediaBrowserCompat.MediaItem> =
        List(playbacks.size) { i -> playbacks[i].toMediaBrowserMediaItem() }

    fun toExoPlayerMediaItemList(): List<com.google.android.exoplayer2.MediaItem> =
        List(playbacks.size) { i -> playbacks[i].toExoPlayerMediaItem() }

    override fun describeContents(): Int = 0

    operator fun get(i: Int): SinglePlayback = playbacks[i]

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist = Playlist(parcel)
        override fun newArray(size: Int): Array<Playlist?> = arrayOfNulls(size)
    }
}