package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.copy

class SpotifyPlaylist(
    override val name: String,
    mediaId: MediaId,
    playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : AbstractPlaylist(mediaId, playbacks, currentPlaylistIndex) {
    override val playbackType = PlaybackType.Playlist.Spotify()

    override fun copy(): Playlist =
        SpotifyPlaylist(name, mediaId, _playbacks.copy(), currentPlaylistIndex)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(mediaId.source)
        parcel.writeParcelableArray(playbacks.toTypedArray(), flags)
        parcel.writeInt(currentPlaylistIndex)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SpotifyPlaylist> {
            override fun createFromParcel(parcel: Parcel): SpotifyPlaylist {
                val name = parcel.readString()!!
                val uri = parcel.readString()!!
                val playbacks = parcel.readParcelableArray(SinglePlayback::class.java.classLoader)!!
                    .map { it as SinglePlayback }.toMutableList()

                val currentPlaylistIndex = parcel.readInt()

                return SpotifyPlaylist(
                    name,
                    MediaId(uri),
                    playbacks,
                    currentPlaylistIndex
                )
            }

            override fun newArray(size: Int): Array<SpotifyPlaylist?> = arrayOfNulls(size)
        }
    }
}