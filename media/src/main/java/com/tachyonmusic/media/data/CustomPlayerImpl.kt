package com.tachyonmusic.media.data

import android.os.Looper
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import androidx.media3.session.CommandButton
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.media.domain.CustomPlayer

/**
 * Override player to always enable SEEK_PREVIOUS and SEEK_NEXT commands
 */
class CustomPlayerImpl(player: Player) : ForwardingPlayer(player), CustomPlayer {
    private var loopMessage: PlayerMessage? = null


    override fun getAvailableCommands(): Player.Commands {
        return wrappedPlayer.availableCommands.run {
            if (mediaItemCount > 1) {
                return@run buildUpon()
                    .add(COMMAND_SEEK_TO_NEXT)
                    .add(COMMAND_SEEK_TO_PREVIOUS)
                    .build()
            }
            this
        }
    }

    // TODO: [COMMAND_SEEK_TO_NEXT/-PREVIOUS] still appears when playing playlist
    override fun isCommandAvailable(command: @Player.Command Int): Boolean {
        return wrappedPlayer.isCommandAvailable(command) || (command == COMMAND_SEEK_TO_NEXT && !isPlayingAd && mediaItemCount > 1) ||
                (command == COMMAND_SEEK_TO_PREVIOUS && !isPlayingAd && mediaItemCount > 1)
    }

    fun createMessage(target: PlayerMessage.Target): PlayerMessage {
        if (wrappedPlayer is ExoPlayer) {
            return (wrappedPlayer as ExoPlayer).createMessage(target)
        }
        TODO("createMessage for other types of players")
    }

    override fun seekToNext() {
        if (currentTimeline.isEmpty || isPlayingAd) {
            return
        }

        if (wrappedPlayer.hasNextMediaItem()) {
            // Dispatch to the wrapped player if not at the final media item
            wrappedPlayer.seekToNextMediaItem()
        } else {
            // Seek to the beginning of the playlist if no next media item is present
            wrappedPlayer.seekToDefaultPosition(0)
        }
    }

    override fun seekToPrevious() {
        if (currentTimeline.isEmpty || isPlayingAd) {
            return
        }

        val startTime = mediaMetadata.extras?.getLong(MetadataKeys.StartTime) ?: 0L

        // Seek to either the previous song or to the last one if we don't have a previous one
        if (isCurrentMediaItemLive && !isCurrentMediaItemSeekable || currentPosition <= maxSeekToPreviousPosition + startTime) {
            if (hasPreviousMediaItem())
                seekToPreviousMediaItem()
            else
                wrappedPlayer.seekToDefaultPosition(wrappedPlayer.mediaItemCount - 1)

        } else {
            // If the player position is less than [maxSeekToPreviousPosition], we'll seek to
            // the beginning of the song
            seekTo(startTime)
        }
    }

    override fun cancelLoopMessage() = loopMessage?.cancel()

    override fun postLoopMessage(startTime: Long, endTime: Long) {
        // Cancel any previous messages
        loopMessage?.cancel()

        loopMessage = createMessage { _, payload ->
            seekTo(payload as Long)
        }.apply {
            looper = Looper.getMainLooper()
            deleteAfterDelivery = false
            payload = startTime
            setPosition(endTime)
            send()
        }
    }

    override fun postLoopMessageForPlaylist(endTime: Long) {
        // Cancel any previous messages
        loopMessage?.cancel()

        loopMessage = createMessage { _, _ ->
            seekToNext()
        }.apply {
            looper = Looper.getMainLooper()
            deleteAfterDelivery = true
            setPosition(endTime)
            send()
        }
    }
}