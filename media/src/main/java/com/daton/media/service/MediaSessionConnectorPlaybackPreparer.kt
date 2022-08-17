package com.daton.media.service

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.daton.media.R
import com.daton.media.data.MediaAction
import com.daton.media.playback.Loop
import com.daton.media.playback.Playback
import com.daton.media.playback.Playlist
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

abstract class MediaSessionConnectorPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

    companion object {
        const val TAG: String = "PlaybackPreparerBase"
    }

    open fun onSetPlayback(playback: Playback) {}

    open fun onSetStartTime(startTime: Long) {}

    open fun onSetEndTime(endTime: Long) {}

    open fun onRequestMediaSourceReload() {}

    open fun onLoopsReceived(loops: MutableList<Loop>) {}

    open fun onPlaylistsReceived(playlists: MutableList<Playlist>) {}

    open fun onCombinePlaybackTypesChanged(combine: Boolean) {}

    open fun onRequestPlaybackUpdate() {}

    open fun onCurrentPlaylistIndexChanged(currentPlaylistIndex: Int) {}

    fun getCustomActions(): Array<out MediaSessionConnector.CustomActionProvider> {
        return arrayOf(
            CustomActionSetPlayback(),
            CustomActionSetStartTime(),
            CustomActionSetEndTime(),
            CustomActionRequestMediaSourceReload(),
            CustomActionSendLoops(),
            CustomActionSendPlaylists(),
            CustomActionCombinePlaybackTypesChanged(),
            CustomActionRequestPlaybackUpdate(),
            CustomActionCurrentPlaylistIndexChanged()
        )
    }

    inner class CustomActionSetPlayback : MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetMediaId.onCustomAction with action $action"
            )
            onSetPlayback(extras!!.getParcelable(MediaAction.Playback)!!)
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SetPlaybackEvent,
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
                MediaAction.SetStartTimeEvent,
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
                MediaAction.SetEndTimeEvent,
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
                MediaAction.RequestMediaSourceReloadEvent,
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

            onLoopsReceived(extras!!.getParcelableArrayList(MediaAction.Loops)!!)
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SendLoopsEvent,
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

            onPlaylistsReceived(extras!!.getParcelableArrayList(MediaAction.Playlists)!!)
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.SendPlaylistsEvent,
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
                MediaAction.CombinePlaybackTypesChangedEvent,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionRequestPlaybackUpdate :
        MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )

            onRequestPlaybackUpdate()
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.RequestPlaybackUpdateEvent,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }

    inner class CustomActionCurrentPlaylistIndexChanged :
        MediaSessionConnector.CustomActionProvider {
        override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
            Log.d(
                TAG,
                "CustomActionSetEndTime.onCustomAction with action $action"
            )

            onCurrentPlaylistIndexChanged(extras!!.getInt(MediaAction.CurrentPlaylistIndex))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                MediaAction.CurrentPlaylistIndexChangedEvent,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }
}


