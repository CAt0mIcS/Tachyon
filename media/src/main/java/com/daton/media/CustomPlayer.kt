package com.daton.media

import com.daton.media.data.MetadataKeys
import com.daton.media.device.Playback
import com.daton.media.ext.startTime
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ForwardingPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.PlayerMessage

/**
 * Override player to always enable SEEK_PREVIOUS and SEEK_NEXT commands
 */
class CustomPlayer(player: Player) : ForwardingPlayer(player) {

    override fun getAvailableCommands(): Player.Commands {
        return wrappedPlayer
            .availableCommands
            .buildUpon()
            .add(COMMAND_SEEK_TO_NEXT)
            .add(COMMAND_SEEK_TO_PREVIOUS)
            .build()
    }

    override fun isCommandAvailable(command: @Player.Command Int): Boolean {
        return wrappedPlayer.isCommandAvailable(command) || (command == COMMAND_SEEK_TO_NEXT && !isPlayingAd) ||
                (command == COMMAND_SEEK_TO_PREVIOUS && !isPlayingAd)
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
        // TODO: How slow is this? Should we just store [startTime] in [mediaMetadata]?
        val playback = mediaMetadata.extras!!.getParcelable<Playback>(MetadataKeys.Playback)!!
        // Seek to either the previous song or to the last one if we don't have a previous one
        if (isCurrentMediaItemLive && !isCurrentMediaItemSeekable || currentPosition <= maxSeekToPreviousPosition + playback.startTime) {
            if (hasPreviousMediaItem())
                seekToPreviousMediaItem()
            else
                wrappedPlayer.seekToDefaultPosition(wrappedPlayer.mediaItemCount - 1)
        } else {
            // If the player position is less than [maxSeekToPreviousPosition], we'll seek to
            // the beginning of the song
            seekTo(playback.endTime)
        }
    }
}