package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.copy

class RemotePlaylistImpl(
    mediaId: MediaId,
    playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : AbstractPlaylist(mediaId, playbacks, currentPlaylistIndex) {

    override val playbackType = PlaybackType.Playlist.Remote()

    override val name: String
        get() = mediaId.source.replace(playbackType.toString(), "")

    override fun copy(): Playlist =
        RemotePlaylistImpl(mediaId, _playbacks.copy(), currentPlaylistIndex)


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
                playbacks,
                currentPlaylistIndex
            )
    }
}
