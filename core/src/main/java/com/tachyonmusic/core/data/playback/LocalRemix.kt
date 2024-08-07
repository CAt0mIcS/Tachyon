package com.tachyonmusic.core.data.playback

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.Song

/**
 * Currently remixes will always be local because Spotify doesn't have any song customization
 * API. However, the underlying [song] could still be a [SpotifySong]
 */
class LocalRemix(
    mediaId: MediaId,
    song: Song,
    override val timestampCreatedAddedEdited: Long
) : AbstractRemix(mediaId, song) {

    override val playbackType = PlaybackType.Remix.Local()

    override fun copy(): Remix =
        LocalRemix(mediaId, song.copy(), timestampCreatedAddedEdited).let {
            it.bassBoost = bassBoost
            it.virtualizerStrength = virtualizerStrength
            it.equalizerBands = equalizerBands
            it.playbackParameters = playbackParameters
            it.reverb = reverb
            it
        }

    override val name: String
        get() = mediaId.source.replace(playbackType.toString(), "")

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalRemix> {
            override fun createFromParcel(parcel: Parcel): LocalRemix {
                val name = parcel.readString()!!

                val song: Song =
                    parcel.readParcelable(Song::class.java.classLoader)!!
                val timestampCreatedAddedEdited = parcel.readLong()
                return LocalRemix(
                    MediaId.ofLocalRemix(name, song.mediaId),
                    song,
                    timestampCreatedAddedEdited
                ).apply {
                    timingData =
                        parcel.readParcelable(TimingDataController::class.java.classLoader)

                    val bass = parcel.readInt()
                    bassBoost = if (bass == 0) null else bass

                    val virtualizerStrength = parcel.readInt()
                    bassBoost = if (virtualizerStrength == 0) null else virtualizerStrength

                    val equalizerBands = mutableListOf<String>()
                    parcel.readList(equalizerBands, String::class.java.classLoader)
                    this.equalizerBands = equalizerBands.map { EqualizerBand.fromString(it) }

                    playbackParameters =
                        parcel.readParcelable(PlaybackParameters::class.java.classLoader)

                    reverb =
                        parcel.readParcelable(ReverbConfig::class.java.classLoader)
                }
            }

            override fun newArray(size: Int): Array<LocalRemix?> = arrayOfNulls(size)
        }
    }
}