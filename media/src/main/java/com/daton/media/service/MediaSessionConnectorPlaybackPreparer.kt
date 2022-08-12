package com.daton.media.service

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.daton.media.R
import com.daton.media.data.MediaAction
import com.daton.media.data.MediaId
import com.daton.media.device.Loop
import com.daton.media.device.Playlist
import com.daton.media.ext.toMediaId
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

abstract class MediaSessionConnectorPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

    companion object {
        const val TAG: String = "PlaybackPreparerBase"
    }

    open fun onSetMediaId(mediaId: MediaId) {}

    open fun onSetStartTime(startTime: Long) {}

    open fun onSetEndTime(endTime: Long) {}

    open fun onRequestMediaSourceReload() {}

    open fun onLoopsReceived(loops: List<Loop>) {}

    open fun onPlaylistsReceived(playlists: List<Playlist>) {}

    open fun onCombinePlaybackTypesChanged(combine: Boolean) {}

    fun getCustomActions(): Array<out MediaSessionConnector.CustomActionProvider> {
        return arrayOf(
            CustomActionSetMediaId(),
            CustomActionSetStartTime(),
            CustomActionSetEndTime(),
            CustomActionRequestMediaSourceReload(),
            CustomActionSendLoops(),
            CustomActionSendPlaylists(),
            CustomActionCombinePlaybackTypesChanged()
        )
    }

    inner class CustomActionSetMediaId : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetMediaId.onCustomAction with action $action"
            )
            onSetMediaId(extras!!.getString(MediaAction.MediaId)!!.toMediaId())
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SetMediaId,
                javaClass.name,
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
                javaClass.name,
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
                javaClass.name,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionRequestMediaSourceReload : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )
            onRequestMediaSourceReload()
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.RequestMediaSourceReload,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionSendLoops : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )

            onLoopsReceived(extras!!.getStringArray(MediaAction.Loops)!!
                .map { loop: String -> Json.decodeFromString<Loop>(loop) })
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SendLoops,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionSendPlaylists : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )

            onPlaylistsReceived(extras!!.getStringArray(MediaAction.Playlists)!!
                .map { playlist: String -> Json.decodeFromString<Playlist>(playlist) })
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SendPlaylists,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionCombinePlaybackTypesChanged :
        MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )

            onCombinePlaybackTypesChanged(extras!!.getBoolean(MediaAction.CombinePlaybackTypes))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.CombinePlaybackTypesChanged,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }
}


