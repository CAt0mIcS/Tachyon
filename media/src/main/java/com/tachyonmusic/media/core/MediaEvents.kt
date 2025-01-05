@file:SuppressLint("UnsafeOptInUsageError")

package com.tachyonmusic.media.core

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Playback

private const val actionPrefix = "com.tachyonmusic."


sealed interface MediaEvent {
    val command: SessionCommand

    fun toBundle(): Bundle
}

/**
 * Events sent to the MediaPlaybackService by the MediaBrowserController
 */
sealed interface MediaBrowserEvent : MediaEvent

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


data class SessionSyncEvent(
    val currentPlayback: Playback?,
    val currentPlaylist: Playlist?,
    val playWhenReady: Boolean
) : MediaSessionEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putBundle(MetadataKeys.Playback, currentPlayback?.toBundle())
        putBundle(MetadataKeys.Playlist, currentPlaylist?.toBundle())
        putBoolean(MetadataKeys.IsPlaying, playWhenReady)
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            SessionSyncEvent(
                bundle.getBundle(MetadataKeys.Playback)?.let { Playback.fromBundle(it) },
                bundle.getBundle(MetadataKeys.Playlist)?.let { Playlist.fromBundle(it) },
                bundle.getBoolean(MetadataKeys.IsPlaying)
            )

        val command = SessionCommand("${actionPrefix}SESSION_SYNC_EVENT", Bundle.EMPTY)
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


/**
 * Events sent both ways
 */

/**
 * This event is called after already setting the media items ([browser.setMediaItems]) so
 * the currently loaded playlist in the [currentPlayer] is already up to date. This also
 * won't be used to transition between playbacks in the current playlist. Only to update
 * audio effects or timing data.
 *
 * When sending this event from the [MediaPlaybackService] to the [MediaBrowserController] it is
 * used to update the information in the [MediaBrowserController] in case it was destroyed (during reconfigurations)
 */
data class PlaybackUpdateEvent(
    val currentPlayback: Playback?,
    val currentPlaylist: Playlist?,
) : MediaBrowserEvent, MediaSessionEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putBundle(MetadataKeys.Playback, currentPlayback?.toBundle())
        putBundle(MetadataKeys.Playlist, currentPlaylist?.toBundle())
    }

    companion object {
        fun fromBundle(bundle: Bundle) =
            PlaybackUpdateEvent(
                bundle.getBundle(MetadataKeys.Playback)?.let { Playback.fromBundle(it) },
                bundle.getBundle(MetadataKeys.Playlist)?.let { Playlist.fromBundle(it) },
            )

        val command = SessionCommand("${actionPrefix}PLAYBACK_UPDATE_EVENT", Bundle.EMPTY)
    }
}


internal fun MediaSession.dispatchMediaEvent(event: MediaSessionEvent) {
    broadcastCustomCommand(event.command, event.toBundle())
}


internal fun SessionCommand.toMediaBrowserEvent(bundle: Bundle): MediaBrowserEvent = when (this) {
    SetRepeatModeEvent.command -> SetRepeatModeEvent.fromBundle(bundle)
    PlaybackUpdateEvent.command -> PlaybackUpdateEvent.fromBundle(bundle)
    else -> TODO("Invalid session command $customAction")
}

fun SessionCommand.toMediaSessionEvent(bundle: Bundle): MediaSessionEvent = when (this) {
    SessionSyncEvent.command -> SessionSyncEvent.fromBundle(bundle)
    PlaybackUpdateEvent.command -> PlaybackUpdateEvent.fromBundle(bundle)
    AudioSessionIdChangedEvent.command -> AudioSessionIdChangedEvent.fromBundle(bundle)
    else -> TODO("Invalid session command $customAction")
}