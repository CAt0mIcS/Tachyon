package com.tachyonmusic.core.data.playback

import android.os.Bundle
import android.os.Parcel
import androidx.compose.runtime.MutableState
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.core.domain.use_case.GetArtwork
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.*

abstract class AbstractSong(
    final override val mediaId: MediaId,
    final override val title: String,
    final override val artist: String,
    final override val duration: Long,
    private val getArtwork: GetArtwork = GetArtwork()
) : Song, AbstractPlayback() {

    final override var timingData = TimingDataController(emptyList())

    abstract override val playbackType: PlaybackType.Song

    private val _artwork = MutableStateFlow<Artwork?>(null)
    override val artwork: StateFlow<Artwork?>
        get() = _artwork

    override fun unloadArtwork() {
        _artwork.value = null
    }

    override suspend fun loadArtwork(imageSize: Int) = flow<Resource<Unit>> {
        getArtwork(this@AbstractSong, imageSize).map {
            when (it) {
                is Resource.Success -> {
                    _artwork.value = it.data
                    emit(Resource.Success())
                }
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Error -> emit(Resource.Error(it.message))
            }

        }.collect()
    }

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString()
    )

    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setUri(uri)
        setMediaMetadata(toMediaMetadata())
    }.build()

    override fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(true)
        setTitle(title)
        setArtist(artist)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration)

            // Empty here to allow custom setting of timing data
            putParcelable(MetadataKeys.TimingData, TimingDataController())
            putParcelable(MetadataKeys.Playback, this@AbstractSong)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mediaId.source)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration)
    }
}