package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.copy

class LocalPlaylistImpl(
    mediaId: MediaId,
    playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : AbstractPlaylist(mediaId, playbacks, currentPlaylistIndex) {

    override val playbackType = PlaybackType.Playlist.Local()

    override val name: String
        get() = mediaId.source.replace(playbackType.toString(), "")

    override fun copy(): Playlist =
        LocalPlaylistImpl(mediaId, _playbacks.copy(), currentPlaylistIndex)


    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalPlaylistImpl> {
            override fun createFromParcel(parcel: Parcel): LocalPlaylistImpl {
                val name = parcel.readString()!!
                val playbacks = parcel.readParcelableArray(SinglePlayback::class.java.classLoader)!!
                    .map { it as SinglePlayback }.toMutableList()

                val currentPlaylistIndex = parcel.readInt()

                return LocalPlaylistImpl(
                    MediaId.ofLocalPlaylist(name),
                    playbacks,
                    currentPlaylistIndex
                )
            }

            override fun newArray(size: Int): Array<LocalPlaylistImpl?> = arrayOfNulls(size)
        }

        fun build(
            mediaId: MediaId,
            playbacks: MutableList<SinglePlayback>,
            currentPlaylistIndex: Int
        ): Playlist =
            LocalPlaylistImpl(
                mediaId,
                playbacks,
                currentPlaylistIndex
            )
    }
}
