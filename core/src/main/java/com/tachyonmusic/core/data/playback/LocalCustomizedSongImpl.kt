package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Song

class LocalCustomizedSongImpl(
    mediaId: MediaId,
    song: Song
) : AbstractCustomizedSong(mediaId, song) {

    override val playbackType = PlaybackType.CustomizedSong.Local()

    override fun copy(): CustomizedSong =
        LocalCustomizedSongImpl(mediaId, song.copy()).let {
            it.bassBoost = bassBoost
            it.virtualizerStrength = virtualizerStrength
            it.equalizerBandLevels = equalizerBandLevels
            it.playbackParameters = playbackParameters
            it.reverb = reverb
            it
        }

    override val name: String
        get() = mediaId.source.replace(playbackType.toString(), "")

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalCustomizedSongImpl> {
            override fun createFromParcel(parcel: Parcel): LocalCustomizedSongImpl {
                val name = parcel.readString()!!

                val song: Song =
                    parcel.readParcelable(Song::class.java.classLoader)!!
                return LocalCustomizedSongImpl(
                    MediaId.ofLocalCustomizedSong(name, song.mediaId),
                    song
                ).apply {
                    timingData =
                        parcel.readParcelable(TimingDataController::class.java.classLoader)

                    val bass = parcel.readInt()
                    bassBoost = if (bass == 0) null else bass

                    val virtualizerStrength = parcel.readInt()
                    bassBoost = if (virtualizerStrength == 0) null else virtualizerStrength

                    val equalizerBandLevels = mutableListOf<Int>()
                    parcel.readList(equalizerBandLevels, Int::class.java.classLoader)
                    this.equalizerBandLevels = equalizerBandLevels

                    playbackParameters =
                        parcel.readParcelable(PlaybackParameters::class.java.classLoader)

                    reverb =
                        parcel.readParcelable(ReverbConfig::class.java.classLoader)
                }
            }

            override fun newArray(size: Int): Array<LocalCustomizedSongImpl?> = arrayOfNulls(size)
        }
    }
}