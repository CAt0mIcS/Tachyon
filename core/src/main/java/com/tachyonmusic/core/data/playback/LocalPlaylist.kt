package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.copy

/**
 * Playlist which was created locally, might have only local songs, a mix of local and remote (spotify)
 * songs, or only spotify songs.
 */
class LocalPlaylist(
    mediaId: MediaId,
    playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : AbstractPlaylist(mediaId, playbacks, currentPlaylistIndex) {

    override val playbackType = PlaybackType.Playlist.Local()

    override val name: String
        get() = mediaId.source.replace(playbackType.toString(), "")

    override fun copy(): Playlist =
        LocalPlaylist(mediaId, _playbacks.copy(), currentPlaylistIndex)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelableArray(playbacks.toTypedArray(), flags)
        parcel.writeInt(currentPlaylistIndex)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalPlaylist> {
            override fun createFromParcel(parcel: Parcel): LocalPlaylist {
                val name = parcel.readString()!!
                val playbacks = parcel.readParcelableArray(SinglePlayback::class.java.classLoader)!!
                    .map { it as SinglePlayback }.toMutableList()

                val currentPlaylistIndex = parcel.readInt()

                return LocalPlaylist(
                    MediaId.ofLocalPlaylist(name),
                    playbacks,
                    currentPlaylistIndex
                )
            }

            override fun newArray(size: Int): Array<LocalPlaylist?> = arrayOfNulls(size)
        }

        fun build(
            mediaId: MediaId,
            playbacks: MutableList<SinglePlayback>,
            currentPlaylistIndex: Int
        ): Playlist =
            LocalPlaylist(
                mediaId,
                playbacks,
                currentPlaylistIndex
            )
    }
}
