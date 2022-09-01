package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song

class RemoteLoop(
    mediaId: MediaId,
    name: String,
    timingData: ArrayList<TimingData>,
    song: Song
) : Loop(mediaId, name, timingData, song) {

    override val playbackType = PlaybackType.Loop.Remote()

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteLoop> {
            override fun createFromParcel(parcel: Parcel): RemoteLoop {
                val name = parcel.readString()!!

                val timingData = TimingData.fromStringArray(parcel.createStringArray()!!)

                val song: Song = parcel.readParcelable(Song::class.java.classLoader)!!
                return RemoteLoop(
                    MediaId.ofRemoteLoop(name, song.mediaId),
                    name,
                    timingData,
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
                map["timingData"]!! as ArrayList<TimingData>,
                LocalSong.build(mediaId.underlyingMediaId!!)
            )
        }
    }
}