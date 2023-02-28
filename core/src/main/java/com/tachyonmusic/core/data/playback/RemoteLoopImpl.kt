package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration

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
    }
}