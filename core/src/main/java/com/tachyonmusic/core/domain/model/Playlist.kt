package com.tachyonmusic.core.domain.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList
import java.io.File


data class Playlist(
    val name: String,
    val playbacks: MutableList<SinglePlayback> = mutableListOf()
) : Playback() {

    override val mediaId: MediaId = MediaId(this)

    override val path: File?
        get() = current?.path
    override val title: String?
        get() = current?.title
    override val artist: String?
        get() = current?.artist
    override val duration: Long?
        get() = current?.duration
    override val albumArt: Bitmap?
        get() = current?.albumArt

    override var startTime: Long
        get() = current?.startTime ?: 0L
        set(value) {
            current?.startTime = value
        }

    override var endTime: Long
        get() = current?.endTime ?: 0L
        set(value) {
            current?.endTime = value
        }

    var currentPlaylistIndex: Int = 0

    val current: SinglePlayback?
        get() = if (currentPlaylistIndex == -1) null else playbacks[currentPlaylistIndex]

    constructor(
        name: String,
        playbacks: MutableList<SinglePlayback>,
        currentPlaylistIndex: Int
    ) : this(name, playbacks) {
        this.currentPlaylistIndex = currentPlaylistIndex
    }

    constructor(
        mediaId: MediaId,
        playbacks: MutableList<SinglePlayback>,
        currentPlaylistIndex: Int
    ) : this(
        mediaId.source.replace(Type.Playlist.toString(), ""), playbacks, currentPlaylistIndex
    )

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        // TODO: More efficient way to convert Array<Parcelable> to MutableList<SinglePlayback>
        parcel.readParcelableArray(SinglePlayback::class.java.classLoader)!!.let { array ->
            MutableList<SinglePlayback>(array.size) { i ->
                array[i] as SinglePlayback
            }
        }
    ) {
        currentPlaylistIndex = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelableArray(playbacks.toTypedArray(), flags)
        parcel.writeInt(currentPlaylistIndex)
    }

    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setMediaMetadata(toMediaMetadata())
    }.build()

    override fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
        setIsPlayable(true)
    }.build()

    fun toMediaItemList(): ImmutableList<MediaItem> =
        ImmutableList.copyOf(playbacks.map { it.toMediaItem() })

    override fun describeContents(): Int = 0

    operator fun get(i: Int): SinglePlayback = playbacks[i]

    override fun toString(): String = mediaId.toString()

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.source,
        "currPlIdx" to currentPlaylistIndex,
        "playbacks" to playbacks.map { it.toHashMap() }
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Playlist> {
            override fun createFromParcel(parcel: Parcel): Playlist = Playlist(parcel)
            override fun newArray(size: Int): Array<Playlist?> = arrayOfNulls(size)
        }

        fun createFromHashMap(map: HashMap<String, Any?>): Playlist {
            val mediaId = MediaId(map["mediaId"]!! as String)
            val idx = (map["currPlIdx"] as Long).toInt()

            val playbacksMaps = map["playbacks"]!! as ArrayList<HashMap<String, Any?>>

            val playbacks = playbacksMaps.map {
                Playback.createFromHashMap(it) as SinglePlayback
            } as MutableList<SinglePlayback>

            return Playlist(mediaId, playbacks, idx)
        }
    }
}