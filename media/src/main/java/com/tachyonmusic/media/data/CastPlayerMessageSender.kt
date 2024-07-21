package com.tachyonmusic.media.data

import android.os.Handler
import androidx.core.os.postDelayed
import androidx.media3.cast.CastPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.PlayerMessage
import com.tachyonmusic.logger.domain.Logger

@UnstableApi
class CastPlayerMessageSender(
    private val player: CastPlayer,
    private val log: Logger
) : PlayerMessage.Sender {
    private val messageSender = Handler(player.applicationLooper)

    override fun sendMessage(message: PlayerMessage) {
        if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
            message.markAsProcessed(false)
            return
        }
        sendMessageInternal(message)
    }

    private fun sendMessageInternal(msg: PlayerMessage) {
        val delay = if (msg.positionMs > player.contentPosition) {
            msg.positionMs - player.contentPosition
        } else {
            msg.positionMs + player.duration - player.contentPosition
        }

        log.debug("Sending Cast Player message after ${delay}ms")

        messageSender.postDelayed(delay) {
            log.debug("Sending Cast Player message...")
            msg.target.handleMessage(0, msg.payload)
        }
    }
}