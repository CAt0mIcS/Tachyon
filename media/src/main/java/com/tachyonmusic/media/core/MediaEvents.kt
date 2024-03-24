package com.tachyonmusic.media.core

import android.os.Bundle
import androidx.media3.common.Bundleable
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.media.util.parcelable

private const val actionPrefix = "com.tachyonmusic."


sealed interface MediaEvent : Bundleable {
    val command: SessionCommand
}

/**
 * Events sent to the MediaPlaybackService by the MediaBrowserController
 */
sealed interface MediaBrowserEvent : MediaEvent

data class SetTimingDataEvent(
    val timingData: TimingDataController
) : MediaBrowserEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putParcelable(MetadataKeys.TimingData, timingData)
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            SetTimingDataEvent(bundle.parcelable(MetadataKeys.TimingData)!!)

        val command = SessionCommand("${actionPrefix}SET_TIMING_DATA", Bundle.EMPTY)
    }
}

data class SeekToTimingDataIndexEvent(
    val index: Int
) : MediaBrowserEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putInt(MetadataKeys.TimingData, index)
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            SeekToTimingDataIndexEvent(bundle.getInt(MetadataKeys.TimingData))

        val command = SessionCommand("${actionPrefix}SEEK_TO_TIMING_DATA_INDEX", Bundle.EMPTY)
    }
}

data class SetRepeatModeEvent(
    val repeatMode: RepeatMode
) : MediaBrowserEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putInt(MetadataKeys.RepeatMode, repeatMode.id)
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            SetRepeatModeEvent(
                RepeatMode.fromId(
                    bundle.getInt(
                        MetadataKeys.RepeatMode,
                        RepeatMode.All.id
                    )
                )
            )

        const val action = "${actionPrefix}SET_REPEAT_MODE"
        val command = SessionCommand(action, Bundle.EMPTY)
    }
}

fun MediaBrowser.dispatchMediaEvent(event: MediaBrowserEvent) {
    sendCustomCommand(event.command, event.toBundle())
}


/**
 * Events sent to the MediaBrowserController by the MediaPlaybackService
 */
sealed interface MediaSessionEvent : MediaEvent

data class TimingDataUpdatedEvent(
    val timingData: TimingDataController?
) : MediaSessionEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putParcelable(MetadataKeys.TimingData, timingData)
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            TimingDataUpdatedEvent(bundle.parcelable(MetadataKeys.TimingData))

        val command = SessionCommand("${actionPrefix}TIMING_DATA_UPDATED", Bundle.EMPTY)
    }
}

data class StateUpdateEvent(
    val currentPlayback: SinglePlayback?,
    val currentPlaylist: Playlist?,
    val playWhenReady: Boolean,
    val repeatMode: RepeatMode
) : MediaSessionEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putParcelable(MetadataKeys.Playback, currentPlayback)
        putParcelable(MetadataKeys.Playlist, currentPlaylist)
        putBoolean(MetadataKeys.IsPlaying, playWhenReady)
        putInt(MetadataKeys.RepeatMode, repeatMode.id)
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            StateUpdateEvent(
                bundle.parcelable(MetadataKeys.Playback),
                bundle.parcelable(MetadataKeys.Playlist),
                bundle.getBoolean(MetadataKeys.IsPlaying),
                RepeatMode.fromId(bundle.getInt(MetadataKeys.RepeatMode))
            )

        val command = SessionCommand("${actionPrefix}STATE_UPDATE_COMMAND", Bundle.EMPTY)
    }
}

data class AudioSessionIdChangedEvent(
    val audioSessionId: Int
) : MediaSessionEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putInt("AudioSessionId", audioSessionId)
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            AudioSessionIdChangedEvent(
                bundle.getInt("AudioSessionId"),
            )

        val command =
            SessionCommand("${actionPrefix}AUDIO_SESSION_ID_CHANGED_COMMAND", Bundle.EMPTY)
    }
}


internal fun MediaSession.dispatchMediaEvent(event: MediaSessionEvent) {
    broadcastCustomCommand(event.command, event.toBundle())
}


internal fun SessionCommand.toMediaBrowserEvent(bundle: Bundle): MediaBrowserEvent = when (this) {
    SetTimingDataEvent.command -> SetTimingDataEvent.fromBundle(bundle)
    SeekToTimingDataIndexEvent.command -> SeekToTimingDataIndexEvent.fromBundle(bundle)
    SetRepeatModeEvent.command -> SetRepeatModeEvent.fromBundle(bundle)
    else -> TODO("Invalid session command $customAction")
}

fun SessionCommand.toMediaSessionEvent(bundle: Bundle): MediaSessionEvent = when (this) {
    TimingDataUpdatedEvent.command -> TimingDataUpdatedEvent.fromBundle(bundle)
    StateUpdateEvent.command -> StateUpdateEvent.fromBundle(bundle)
    AudioSessionIdChangedEvent.command -> AudioSessionIdChangedEvent.fromBundle(bundle)
    else -> TODO("Invalid session command $customAction")
}