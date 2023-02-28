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