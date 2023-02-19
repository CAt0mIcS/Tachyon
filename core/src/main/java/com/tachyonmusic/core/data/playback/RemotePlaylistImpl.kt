package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback

class RemotePlaylistImpl(
    mediaId: MediaId,
    name: String,
    playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : AbstractPlaylist(mediaId, name, playbacks, currentPlaylistIndex) {

    override val playbackType = PlaybackType.Playlist.Remote()

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemotePlaylistImpl> {
            override fun createFromParcel(parcel: Parcel): RemotePlaylistImpl {
                val name = parcel.readString()!!
                val playbacks = parcel.readParcelableArray(SinglePlayback::class.java.classLoader)!!
                    .map { it as SinglePlayback }.toMutableList()

                val currentPlaylistIndex = parcel.readInt()

                return RemotePlaylistImpl(
                    MediaId.ofRemotePlaylist(name),
                    name,
                    playbacks,
                    currentPlaylistIndex
                )
            }

            override fun newArray(size: Int): Array<RemotePlaylistImpl?> = arrayOfNulls(size)
        }

        fun build(map: Map<String, Any?>): RemotePlaylistImpl {
            val mediaId = MediaId(map["mediaId"]!! as String)
            val idx = (map["currPlIdx"] as Long).toInt()

            val name = mediaId.source.replace(PlaybackType.Playlist.Remote().toString(), "")

            val playbacksMaps = map["playbacks"]!! as ArrayList<Map<String, Any?>>

            val playbacks = playbacksMaps.map { map ->
                val singleMediaId = MediaId.deserialize(map["mediaId"]!! as String)
                if (singleMediaId.isLocalSong)
                    return@map LocalSongImpl.build(map)
                RemoteLoopImpl.build(map)
            } as MutableList<SinglePlayback>

            return RemotePlaylistImpl(mediaId, name, playbacks, idx)
        }

        fun build(
            mediaId: MediaId,
            playbacks: MutableList<SinglePlayback>,
            currentPlaylistIndex: Int
        ): Playlist =
            RemotePlaylistImpl(
                mediaId,
                mediaId.source.replace(PlaybackType.Playlist.Remote().toString(), ""),
                playbacks,
                currentPlaylistIndex
            )

        fun build(
            name: String,
            playbacks: MutableList<SinglePlayback>,
            currentPlaylistIndex: Int
        ): Playlist =
            RemotePlaylistImpl(
                MediaId.ofRemotePlaylist(name),
                name,
                playbacks,
                currentPlaylistIndex
            )
    }
}