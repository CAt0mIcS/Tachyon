package com.tachyonmusic.media.data

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.PlayerMessage

@OptIn(UnstableApi::class)
class CastPlayerMessageSender : PlayerMessage.Sender {
    override fun sendMessage(message: PlayerMessage) {

    }
}