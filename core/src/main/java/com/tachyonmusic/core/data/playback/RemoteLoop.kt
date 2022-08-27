package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song

class RemoteLoop(
    mediaId: MediaId,
    name: String,
    startTime: Long,
    endTime: Long,
    song: Song
) : Loop(mediaId, name, startTime, endTime, song) {

    override val playbackType = PlaybackType.Loop.Remote()

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteLoop> {
            override fun createFromParcel(parcel: Parcel): RemoteLoop {
                val name = parcel.readString()!!
                val startTime = parcel.readLong()
                val endTime = parcel.readLong()
                val song: Song = parcel.readParcelable(Song::class.java.classLoader)!!
                return RemoteLoop(
                    MediaId.ofRemoteLoop(name, song.mediaId),
                    name,
                    startTime,
                    endTime,
                    song
                )
            }

            override fun newArray(size: Int): Array<RemoteLoop?> = arrayOfNulls(size)
        }

        fun build(map: HashMap<String, Any?>): Loop {
            val mediaId = MediaId.deserialize(map["mediaId"]!! as String)
            val name = mediaId.source.replace(PlaybackType.Loop.Remote().toString(), "")
            return RemoteLoop(
                mediaId,
                name,
                map["startTime"]!! as Long,
                map["endTime"]!! as Long,
                LocalSong.build(mediaId.underlyingMediaId!!)
            )
        }
    }
}