package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
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

        fun build(map: HashMap<String, Any?>): RemotePlaylist {
            val mediaId = MediaId(map["mediaId"]!! as String)
            val idx = (map["currPlIdx"] as Long).toInt()

            val name = mediaId.source.replace(PlaybackType.Playlist.Remote().toString(), "")

            val playbacksMaps = map["playbacks"]!! as ArrayList<HashMap<String, Any?>>

            val playbacks = playbacksMaps.map { map ->
                val tag = map["type"]!! as Int
                if (tag == PlaybackType.Song.Local().value)
                    return@map LocalSong.build(map)
                RemoteLoop.build(map)
            } as MutableList<SinglePlayback>

            return RemotePlaylist(mediaId, name, playbacks, idx)
        }
    }
}