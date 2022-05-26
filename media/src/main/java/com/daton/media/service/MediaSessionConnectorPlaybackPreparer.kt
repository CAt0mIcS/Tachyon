package com.daton.media.service

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.daton.media.R
import com.daton.media.MediaAction
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

abstract class MediaSessionConnectorPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

    companion object {
        const val TAG: String = "PlaybackPreparerBase"
    }

    open fun onSetMediaId(mediaId: String) {}

    open fun onSetStartTime(startTime: Long) {}

    open fun onSetEndTime(endTime: Long) {}

    open fun onStoragePermissionChanged(permissionGranted: Boolean) {}

    open fun onLoopReceived(mediaId: String, songMediaId: String, startTime: Long, endTime: Long) {}

    open fun onPlaylistReceived(mediaId: String, mediaIds: Array<String>) {}

    fun getCustomActions(): Array<out MediaSessionConnector.CustomActionProvider> {
        return arrayOf(
            CustomActionSetMediaId(),
            CustomActionSetStartTime(),
            CustomActionSetEndTime(),
            CustomActionStoragePermissionChanged(),
            CustomActionSendLoop(),
            CustomActionSendPlaylist()
        )
    }

    inner class CustomActionSetMediaId : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetMediaId.onCustomAction with action $action"
            )
            onSetMediaId(extras!!.getString(MediaAction.MediaId)!!)
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SetMediaId,
                MediaAction.MediaId,
                R.drawable.music_note // TODO: WHY????
            ).build()
    }

    inner class CustomActionSetStartTime : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetStartTime.onCustomAction with action $action"
            )
            onSetStartTime(extras!!.getLong(MediaAction.StartTime))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SetStartTime,
                MediaAction.StartTime,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionSetEndTime : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )
            onSetEndTime(extras!!.getLong(MediaAction.EndTime))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SetEndTime,
                MediaAction.EndTime,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionStoragePermissionChanged : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )
            onStoragePermissionChanged(extras!!.getBoolean(MediaAction.StoragePermissionGranted))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.StoragePermissionChanged,
                MediaAction.StoragePermissionGranted,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionSendLoop : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )
            onLoopReceived(
                extras!!.getString(MediaAction.MediaId)!!,
                extras.getString(MediaAction.SongMediaId)!!,
                extras.getLong(MediaAction.StartTime),
                extras.getLong(MediaAction.EndTime)
            )
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SendLoop,
                MediaAction.SendLoop,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionSendPlaylist : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )
            onPlaylistReceived(
                extras!!.getString(MediaAction.MediaId)!!,
                extras.getStringArray(MediaAction.MediaIds)!!
            )
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SendPlaylist,
                MediaAction.SendPlaylist,
                R.drawable.music_note
            ).build()
    }
}


