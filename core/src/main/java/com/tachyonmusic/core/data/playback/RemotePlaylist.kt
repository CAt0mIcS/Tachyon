package com.tachyonmusic.core.data.playback

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.internal.LinkedTreeMap
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback

class RemotePlaylist(
    mediaId: MediaId,
    name: String,
    playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : Playlist(mediaId, name, playbacks, currentPlaylistIndex) {

    override val playbackType = PlaybackType.Playlist.Remote()

    override suspend fun loadBitmap(onDone: suspend () -> Unit) {
        for (playback in playbacks)
            playback.loadBitmap()
        onDone()
    }


    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemotePlaylist> {
            override fun createFromParcel(parcel: Parcel): RemotePlaylist {
                val name = parcel.readString()!!
                // TODO: More efficient way to convert Array<Parcelable> to MutableList<SinglePlayback>
                val playbacks =
                    parcel.readParcelableArray(SinglePlayback::class.java.classLoader)!!
                        .let { array ->
                            MutableList<SinglePlayback>(array.size) { i ->
                                array[i] as SinglePlayback
                            }
                        }

                val currentPlaylistIndex = parcel.readInt()

                return RemotePlaylist(
                    MediaId.ofRemotePlaylist(name),
                    name,
                    playbacks,
                    currentPlaylistIndex
                )
            }

            override fun newArray(size: Int): Array<RemotePlaylist?> = arrayOfNulls(size)
        }

        fun build(map: Map<String, Any?>): RemotePlaylist {
            val mediaId = MediaId(map["mediaId"]!! as String)
            val idx = (map["currPlIdx"] as Long).toInt()

            val name = mediaId.source.replace(PlaybackType.Playlist.Remote().toString(), "")

            val playbacksMaps = map["playbacks"]!! as ArrayList<Map<String, Any?>>

            val playbacks = playbacksMaps.map { map ->
                val singleMediaId = MediaId.deserialize(map["mediaId"]!! as String)
                if (singleMediaId.isLocalSong)
                    return@map LocalSong.build(map)
                RemoteLoop.build(map)
            } as MutableList<SinglePlayback>

            return RemotePlaylist(mediaId, name, playbacks, idx)
        }

        fun build(
            mediaId: MediaId,
            playbacks: MutableList<SinglePlayback>,
            currentPlaylistIndex: Int
        ) =
            RemotePlaylist(
                mediaId,
                mediaId.source.replace(PlaybackType.Playlist.Remote().toString(), ""),
                playbacks,
                currentPlaylistIndex
            )
    }
}