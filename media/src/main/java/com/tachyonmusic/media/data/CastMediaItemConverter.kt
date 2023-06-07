package com.tachyonmusic.media.data

import android.net.Uri
import android.os.Bundle
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.MimeTypes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.ext.toBoolean
import com.tachyonmusic.core.data.ext.toInt
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.media.domain.CastWebServerController
import com.tachyonmusic.media.util.duration
import com.tachyonmusic.media.util.name
import org.json.JSONObject


class CastMediaItemConverter(
    private val castWebServerController: CastWebServerController
) : MediaItemConverter {
    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val metadata = MediaMetadata(MEDIA_TYPE_MUSIC)
        val oldMetadata = mediaItem.mediaMetadata

        if (oldMetadata.title != null) {
            metadata.putString(MediaMetadata.KEY_TITLE, oldMetadata.title!!.toString())
        }
        if (oldMetadata.artist != null) {
            metadata.putString(MediaMetadata.KEY_ARTIST, oldMetadata.artist!!.toString())
        }
        if (oldMetadata.artworkUri != null) {
            metadata.addImage(WebImage(oldMetadata.artworkUri!!))
        }
        if (oldMetadata.isPlayable != null) {
            metadata.putInt(KEY_IS_PLAYABLE, oldMetadata.isPlayable!!.toInt())
        }
        if (oldMetadata.name != null) {
            metadata.putString(KEY_NAME, oldMetadata.name!!)
        }
        if (oldMetadata.folderType != null) {
            metadata.putInt(KEY_FOLDER_TYPE, oldMetadata.folderType!!)
        }

        metadata.putString(KEY_MEDIA_ID, mediaItem.mediaId)

        return MediaQueueItem.Builder(
            MediaInfo.Builder(mediaItem.mediaId)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.AUDIO_MPEG) // TODO: Different mime types for different file extensions
                .setContentUrl(castWebServerController.getUrl(mediaItem.localConfiguration!!.uri))
                .setStreamDuration(oldMetadata.duration!!.inWholeMilliseconds)
                .setCustomData(getCustomData(mediaItem))
                .setMetadata(metadata)
                .build()
        ).build()
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        val mediaInfo = mediaQueueItem.media!!
        val metadata = mediaInfo.metadata!!

        val customData = parseCustomData(mediaInfo.customData)

        val mediaMetadata = androidx.media3.common.MediaMetadata.Builder().apply {
            setIsPlayable(metadata.getInt(KEY_IS_PLAYABLE).toBoolean())
            setFolderType(metadata.getInt(KEY_FOLDER_TYPE))

            setTitle(metadata.getString(MediaMetadata.KEY_TITLE))
            setArtist(metadata.getString(MediaMetadata.KEY_ARTIST))
            setArtworkUri(metadata.images.firstOrNull()?.url)

            setExtras(Bundle().apply {
                putString(MetadataKeys.Name, metadata.getString(KEY_NAME))
                putLong(MetadataKeys.Duration, mediaInfo.streamDuration)
                putParcelable(MetadataKeys.TimingData, customData?.timingData)
                putParcelable(MetadataKeys.Playback, customData?.playback)
            })
        }.build()


        val mediaId = mediaInfo.contentId
        val uri = Uri.parse(mediaInfo.contentUrl)

        return MediaItem.Builder().apply {
            setMediaId(mediaId)
            setUri(uri)
            setMediaMetadata(mediaMetadata)
        }.build()
    }

    private fun getCustomData(mediaItem: MediaItem): JSONObject? {
        return null
    }

    private fun parseCustomData(json: JSONObject?): CustomData? {
        return null
    }

    companion object {
        const val KEY_NAME = "com.tachyonmusic.NAME"
        const val KEY_IS_PLAYABLE = "com.tachyonmusic.IS_PLAYABLE"
        const val KEY_MEDIA_ID = "com.tachyonmusic.MEDIA_ID"
        const val KEY_FOLDER_TYPE = "com.tachyonmusic.FOLDER_TYPE"
    }

    private data class CustomData(
        val timingData: TimingDataController,
        val playback: Playback
    )
}