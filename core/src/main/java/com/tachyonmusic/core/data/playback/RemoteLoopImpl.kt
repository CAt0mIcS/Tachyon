package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song

class RemoteLoopImpl(
    mediaId: MediaId,
    name: String,
    timingData: TimingDataController,
    song: Song
) : AbstractLoop(mediaId, name, timingData, song) {

    override val playbackType = PlaybackType.Loop.Remote()

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteLoopImpl> {
            override fun createFromParcel(parcel: Parcel): RemoteLoopImpl {
                val name = parcel.readString()!!

                val timingData: TimingDataController =
                    parcel.readParcelable(TimingDataController::class.java.classLoader)!!

                val song: Song =
                    parcel.readParcelable(Song::class.java.classLoader)!!
                return RemoteLoopImpl(
                    MediaId.ofRemoteLoop(name, song.mediaId),
                    name,
                    timingData,
                    song
                )
            }

            override fun newArray(size: Int): Array<RemoteLoopImpl?> = arrayOfNulls(size)
        }

        fun build(map: Map<String, Any?>): Loop {
            val mediaId = MediaId.deserialize(map["mediaId"]!! as String)
            val name = mediaId.source.replace(PlaybackType.Loop.Remote().toString(), "")
            return RemoteLoopImpl(
                mediaId,
                name,
                TimingDataController(map["timingData"]!! as ArrayList<String>),
                LocalSongImpl.build(mediaId.underlyingMediaId!!)
            )
        }

        fun build(mediaId: MediaId, timingData: TimingDataController): Loop = RemoteLoopImpl(
            mediaId,
            mediaId.source.replace(PlaybackType.Loop.Remote().toString(), ""),
            timingData,
            if (mediaId.underlyingMediaId?.isLocalSong == true) LocalSongImpl.build(mediaId.underlyingMediaId) else TODO(
                "Unknown song type when deserializing loop"
            )
        )

        fun build(name: String, songMediaId: MediaId, timingData: TimingDataController) =
            build(MediaId.ofRemoteLoop(name, songMediaId), timingData)
    }
}