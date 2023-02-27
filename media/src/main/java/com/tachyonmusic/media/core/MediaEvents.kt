package com.tachyonmusic.media.core

import android.os.Bundle
import androidx.media3.common.Bundleable
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.media.util.parcelable

private const val actionPrefix = "com.tachyonmusic."

sealed interface MediaEvent : Bundleable {
    val command: SessionCommand
}

/**
 * Events sent to the MediaPlaybackService by the MediaBrowserController
 */
sealed interface MediaBrowserEvent : MediaEvent

/**
 * Events sent to the MediaBrowserController by the MediaPlaybackService
 */
sealed interface MediaSessionEvent : MediaEvent


data class SetPlaybackEvent(
    val playback: Playback?
) : MediaBrowserEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putParcelable(MetadataKeys.Playback, playback)
    }

    companion object {
        fun fromBundle(bundle: Bundle) = SetPlaybackEvent(bundle.parcelable(MetadataKeys.Playback))
        val command = SessionCommand("${actionPrefix}SET_PLAYBACK", Bundle.EMPTY)
    }
}

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
            SetRepeatModeEvent(RepeatMode.fromId(bundle.getInt(MetadataKeys.RepeatMode)))

        val command = SessionCommand("${actionPrefix}SET_REPEAT_MODE", Bundle.EMPTY)
    }
}

data class SetSortingParamsEvent(
    val sortParameters: SortParameters
) : MediaBrowserEvent {
    override val command: SessionCommand
        get() = Companion.command

    override fun toBundle() = Bundle().apply {
        putInt(MetadataKeys.SortType, sortParameters.type.ordinal)
        putInt(MetadataKeys.SortOrder, sortParameters.order.ordinal)
    }

    companion object {
        fun fromBundle(bundle: Bundle) = SetSortingParamsEvent(
            SortParameters(
                SortType.fromInt(bundle.getInt(MetadataKeys.SortType)),
                SortOrder.fromInt(bundle.getInt(MetadataKeys.SortOrder))
            )
        )

        val command = SessionCommand("${actionPrefix}SET_SORTING_PARAMS", Bundle.EMPTY)
    }
}

fun MediaBrowser.dispatchMediaEvent(event: MediaBrowserEvent) {
    sendCustomCommand(event.command, event.toBundle())
}


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


internal fun MediaSession.dispatchMediaEvent(event: MediaSessionEvent) {
    broadcastCustomCommand(event.command, event.toBundle())
}


internal fun SessionCommand.toMediaBrowserEvent(bundle: Bundle): MediaBrowserEvent = when (this) {
    SetPlaybackEvent.command -> SetPlaybackEvent.fromBundle(bundle)
    SetTimingDataEvent.command -> SetTimingDataEvent.fromBundle(bundle)
    SetRepeatModeEvent.command -> SetRepeatModeEvent.fromBundle(bundle)
    SetSortingParamsEvent.command -> SetSortingParamsEvent.fromBundle(bundle)
    else -> TODO("Invalid session command $customAction")
}

fun SessionCommand.toMediaSessionEvent(bundle: Bundle): MediaSessionEvent = when (this) {
    TimingDataUpdatedEvent.command -> TimingDataUpdatedEvent.fromBundle(bundle)
    else -> TODO("Invalid session command $customAction")
}