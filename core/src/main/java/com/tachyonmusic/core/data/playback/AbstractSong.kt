package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.ext.toInt
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbstractSong(
    final override val mediaId: MediaId,
    final override val title: String,
    final override val artist: String,
    final override val duration: Duration,
) : Song, AbstractPlayback() {

    final override var timingData: TimingDataController? = TimingDataController(emptyList())

    override var artworkType = ArtworkType.UNKNOWN
    abstract override val playbackType: PlaybackType.Song

    override val isPlayable = MutableStateFlow(false)

    override val artwork = MutableStateFlow<Artwork?>(null)
    override val isArtworkLoading = MutableStateFlow(false)

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString()
    )

    // TODO: Remove
    private var ongoingArtworkJob: Job? = null

    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setUri(uri)
        setMediaMetadata(toMediaMetadata())
    }.build()

    private fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(isPlayable.value)

        // EmbeddedArtwork automatically handled by media3
        when (val artworkVal = artwork.value) {
            null -> {}
            is RemoteArtwork -> setArtworkUri(Uri.parse(artworkVal.uri.toURL().toString()))
        }

        setTitle(title)
        setArtist(artist)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration.inWholeMilliseconds)

            // Empty here to allow custom setting of timing data
            putParcelable(MetadataKeys.TimingData, TimingDataController())
            putParcelable(MetadataKeys.Playback, this@AbstractSong)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        assert(ongoingArtworkJob == null || ongoingArtworkJob!!.isCompleted)

        parcel.writeParcelable(uri, flags)
        parcel.writeString(mediaId.source)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration.inWholeMilliseconds)
        parcel.writeParcelable(artwork.value, flags)
        parcel.writeInt(isArtworkLoading.value.toInt())
        parcel.writeInt(isPlayable.value.toInt())
        parcel.writeString(artworkType)
    }


    override suspend fun loadArtworkAsync(
        resourceFlow: Flow<Resource<Artwork>>,
        onCompletion: suspend (MediaId?, Artwork?) -> Unit
    ): Unit = withContext(Dispatchers.IO) {
        ongoingArtworkJob = launch {
            resourceFlow.onEach { res ->
                when (res) {
                    is Resource.Loading -> isArtworkLoading.update { true }
                    else -> {
                        isArtworkLoading.update { false }
                        artwork.update { res.data }
                        onCompletion(mediaId, res.data)
                    }
                }
            }.collect()
        }
    }
}