package com.tachyonmusic.media.service

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.tachyonmusic.media.ext.albumArt
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.tachyonmusic.media.R

class NotificationManager(
    context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager =
            PlayerNotificationManager.Builder(context, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
                .run {
                    setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
                    setNotificationListener(notificationListener)
                    setChannelNameResourceId(R.string.notification_channel)
                    setChannelDescriptionResourceId(R.string.notification_channel_description)

                    return@run build()
                }

        notificationManager.run {
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.music_note)

            // TODO: Set fast forward/rewind action icons to custom ones and decide if we want it to show in compact view

            setUseRewindAction(true)
            setUseFastForwardAction(true)
            setUseFastForwardActionInCompactView(false)
            setUseRewindActionInCompactView(false)

            setUsePreviousActionInCompactView(true)
            setUseNextActionInCompactView(true)
        }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player) {
        notificationManager.setPlayer(player)
    }


    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            controller.sessionActivity

        override fun getCurrentContentText(player: Player) = player.mediaMetadata.artist.toString()

        override fun getCurrentContentTitle(player: Player) = player.mediaMetadata.title.toString()

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return controller.metadata.albumArt
            // TODO: Download missing album art here
            // TODO: Above functions previously returned null (controller.metadata.title/artist), this will always return null, too
        }
    }

}

const val NOTIFICATION_CHANNEL_ID = "com.tachyonmusic/PLAYBACK_NOTIFICATION"
const val NOTIFICATION_ID = 1337