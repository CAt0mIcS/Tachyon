package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song

class RemoteLoopImpl(
    mediaId: MediaId,
    timingData: TimingDataController?,
    song: Song
) : AbstractLoop(mediaId, timingData, song) {

    override val playbackType = PlaybackType.Loop.Remote()

    override fun copy(): Loop = RemoteLoopImpl(mediaId, timingData?.copy(), song.copy())

    override val name: String
        get() = mediaId.source.replace(playbackType.toString(), "")

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
                    timingData,
                    song
                )
            }

            override fun newArray(size: Int): Array<RemoteLoopImpl?> = arrayOfNulls(size)
        }
    }
}