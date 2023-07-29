package com.tachyonmusic.media.data

import android.os.Looper
import androidx.media3.cast.CastPlayer
import androidx.media3.common.*
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.util.timingData
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.ms

/**
 * Override player to always enable SEEK_PREVIOUS and SEEK_NEXT commands
 */
class CustomPlayerImpl(
    player: Player,
    private val log: Logger
) : ReplaceableForwardingPlayer(player),
    CustomPlayer,
    Player.Listener,
    IListenable<CustomPlayer.Listener> by Listenable() {

    private var customizedSongMessage: PlayerMessage? = null
    private var castPlayerMessageSender: CastPlayerMessageSender? = null

    init {
        addListener(this)
    }

    override fun getAvailableCommands(): Player.Commands {
        return player.availableCommands.run {
            if (mediaItemCount > 1) {
                return@run buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .build()
            }
            this
        }
    }

    // TODO: [COMMAND_SEEK_TO_NEXT/-PREVIOUS] still appears when playing playlist
    override fun isCommandAvailable(command: @Player.Command Int): Boolean {
        return player.isCommandAvailable(command) || (command == Player.COMMAND_SEEK_TO_NEXT && !isPlayingAd && mediaItemCount > 1) ||
                (command == Player.COMMAND_SEEK_TO_PREVIOUS && !isPlayingAd && mediaItemCount > 1)
    }

    override var audioSessionId: Int
        get() = if (player is ExoPlayer) (player as ExoPlayer).audioSessionId else 0
        set(value) {
            if (player is ExoPlayer) (player as ExoPlayer).audioSessionId =
                value else TODO(
                "audioSessionId not valid on another player yet"
            )
        }

    fun createMessage(target: PlayerMessage.Target) =
        when (player) {
            is ExoPlayer -> (player as ExoPlayer).createMessage(target)
            is CastPlayer -> {
                if (castPlayerMessageSender == null)
                    castPlayerMessageSender = CastPlayerMessageSender(player as CastPlayer, log)

                PlayerMessage(
                    castPlayerMessageSender!!,
                    target,
                    currentTimeline,
                    if (currentMediaItemIndex == C.INDEX_UNSET) 0 else currentMediaItemIndex,
                    Clock.DEFAULT,
                    player.applicationLooper // TODO (internalPlayer.getPlaybackLooper())
                )
            }

            else -> TODO("createMessage for other types of players")
        }

    override fun indexOfMediaItem(mediaId: MediaId): Int {
        for (i in 0 until mediaItemCount) {
            if (MediaId.deserializeIfValid(getMediaItemAt(i).mediaId) == mediaId)
                return i
        }
        return -1
    }

    override fun seekToNext() {
        if (currentTimeline.isEmpty || isPlayingAd) {
            return
        }

        if (player.hasNextMediaItem()) {
            // Dispatch to the wrapped player if not at the final media item
            player.seekToNextMediaItem()
        } else {
            // Seek to the beginning of the playlist if no next media item is present
            player.seekToDefaultPosition(0)
        }
    }

    override fun seekToPrevious() {
        if (currentTimeline.isEmpty || isPlayingAd) {
            return
        }

        // Only the first start time matters when choosing if we play the previous playback
        // or seek back to the beginning of the current one
        val startTime = mediaMetadata.timingData?.getOrNull(0)?.startTime?.inWholeMilliseconds ?: 0L

        // Seek to either the previous song or to the last one if we don't have a previous one
        if (isCurrentMediaItemLive && !isCurrentMediaItemSeekable || currentPosition <= maxSeekToPreviousPosition + startTime) {
            if (hasPreviousMediaItem())
                seekToPreviousMediaItem()
            else
                player.seekToDefaultPosition(player.mediaItemCount - 1)

        } else {
            // If the player position is less than [maxSeekToPreviousPosition], we'll seek to
            // the beginning of the song
            seekTo(startTime)
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        /**
         * When seeking we need to update the [currentTimingDataIndex] depending on the seek position
         */
        onMediaItemTransition(currentMediaItem, Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val timingData = mediaItem?.mediaMetadata?.timingData
        if (timingData != null && timingData.isNotEmpty()) {
            /**
             * TODO:
             *  When a new playback is played, we don't want to call [TimingDataController.advanceToCurrentPosition]
             *  because at position 0ms (starting pos) the second/third/... timing data might be closer to position 0ms
             *  than the first one in the timing data array. But when starting a new song we want to play
             *  the first timing data in the list. Currently we handle this in [TimingDataController.advanceToCurrentPosition]
             *  by checking if the position is 0ms and returning index 0 in this case
             */
            updateTimingData(timingData)
        }
    }

    override fun updateTimingData(newTimingData: TimingDataController) {
        if (newTimingData.size == 0 || newTimingData.size == 1 && newTimingData.coversDuration(
                0.ms,
                duration.ms
            )
        ) {
            currentMediaItem?.mediaMetadata?.timingData = null
            return
        }

        newTimingData.advanceToCurrentPosition(currentPosition.ms)
        postCustomizedSongMessage(
            newTimingData.next.startTime,
            newTimingData.current.endTime
        )
        // Only seek if we're not in any timing data interval
        if (!newTimingData.anySurrounds(currentPosition.ms))
            seekWithoutCallback(newTimingData.current.startTime.inWholeMilliseconds)

        currentMediaItem?.mediaMetadata?.timingData = newTimingData
        invokeEvent { it.onTimingDataUpdated(newTimingData) }
    }

    override fun stop() {
        if (currentMediaItem?.mediaMetadata?.timingData?.isNotEmpty() == true) {
            // already handled by timing data message
        } else
            seekToDefaultPosition()
        super.stop()
        playWhenReady = false
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if(playbackState == Player.STATE_ENDED) {
            if (currentMediaItem?.mediaMetadata?.timingData?.isNotEmpty() == true) {
                // already handled by timing data message
            } else
                seekToDefaultPosition()
            playWhenReady = false
        }
    }

    override fun setAuxEffectInfo(info: AuxEffectInfo) {
        if (player is ExoPlayer)
            (player as ExoPlayer).setAuxEffectInfo(info)
        else
            TODO("Cannot set aux effect info in another player yet")
    }

    private fun postCustomizedSongMessage(startTime: Duration, endTime: Duration) {
        // Cancel any previous messages
        customizedSongMessage?.cancel()

        customizedSongMessage = createMessage { _, payload ->
            seekWithoutCallback(payload as Long)
            val timingData = currentMediaItem?.mediaMetadata?.timingData
            if (timingData != null) {
                if (timingData.currentIndex + 1 == timingData.size) {
                    if (repeatMode == Player.REPEAT_MODE_ALL) {
                        log.debug("Timing data end reached, seeking to next playback item...")
                        seekToNext()
                        return@createMessage
                    } else if (repeatMode == Player.REPEAT_MODE_OFF) {
                        log.debug("Timing data end reached, seeking to next playback item or stopping playback due to repeatMode=RepeatMode.Off...")
                        if (player.hasNextMediaItem())
                            seekToNext()
                        else
                            stop()
                        return@createMessage
                    }
                }

                timingData.advanceToNext()
                invokeEvent { it.onTimingDataUpdated(timingData) }
                postCustomizedSongMessage(timingData.next.startTime, timingData.current.endTime)
                log.debug(
                    "The next timing data will be loaded at ${timingData.current.endTime} and will seek to ${timingData.next.startTime}"
                )
            }
        }.apply {
            looper = Looper.getMainLooper()
            deleteAfterDelivery = false
            payload = startTime.inWholeMilliseconds
            setPosition(endTime.inWholeMilliseconds)
            send()
        }
    }

    private val registerOldSeekHandlerCallback = object : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            removeListener(this)
            addListener(this@CustomPlayerImpl)
        }
    }

    private fun seekWithoutCallback(positionMs: Long) {
        removeListener(this)
        addListener(registerOldSeekHandlerCallback)
        seekTo(positionMs)
    }
}