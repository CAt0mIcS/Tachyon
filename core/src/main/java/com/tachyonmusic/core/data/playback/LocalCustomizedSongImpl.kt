package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Song

class LocalCustomizedSongImpl(
    mediaId: MediaId,
    timingData: TimingDataController?,
    song: Song
) : AbstractCustomizedSong(mediaId, timingData, song) {

    override val playbackType = PlaybackType.CustomizedSong.Local()

    override fun copy(): CustomizedSong = LocalCustomizedSongImpl(mediaId, timingData?.copy(), song.copy())

    override val name: String
        get() = mediaId.source.replace(playbackType.toString(), "")

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalCustomizedSongImpl> {
            override fun createFromParcel(parcel: Parcel): LocalCustomizedSongImpl {
                val name = parcel.readString()!!

                val timingData: TimingDataController =
                    parcel.readParcelable(TimingDataController::class.java.classLoader)!!

                val song: Song =
                    parcel.readParcelable(Song::class.java.classLoader)!!
                return LocalCustomizedSongImpl(
                    MediaId.ofLocalCustomizedSong(name, song.mediaId),
                    timingData,
                    song
                )
            }

            override fun newArray(size: Int): Array<LocalCustomizedSongImpl?> = arrayOfNulls(size)
        }
    }
}