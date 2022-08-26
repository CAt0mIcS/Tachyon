package com.tachyonmusic.media.service

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.tachyonmusic.media.R
import com.tachyonmusic.core.constants.MediaAction
import com.tachyonmusic.core.domain.model.Loop
import com.tachyonmusic.core.domain.model.Playback
import com.tachyonmusic.core.domain.model.Playlist
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

abstract class MediaSessionConnectorPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

    companion object {
        const val TAG: String = "PlaybackPreparerBase"
    }

    open fun onSetPlayback(playback: com.tachyonmusic.core.domain.model.Playback) {}

    open fun onSetStartTime(startTime: Long) {}

    open fun onSetEndTime(endTime: Long) {}

    open fun onRequestMediaSourceReload() {}

    open fun onLoopsReceived(loops: MutableList<com.tachyonmusic.core.domain.model.Loop>) {}

    open fun onPlaylistsReceived(playlists: MutableList<com.tachyonmusic.core.domain.model.Playlist>) {}

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
            onSetPlayback(extras!!.getParcelable(com.tachyonmusic.core.constants.MediaAction.Playback)!!)
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                com.tachyonmusic.core.constants.MediaAction.SetPlaybackEvent,
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
            onSetStartTime(extras!!.getLong(com.tachyonmusic.core.constants.MediaAction.StartTime))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                com.tachyonmusic.core.constants.MediaAction.SetStartTimeEvent,
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
            onSetEndTime(extras!!.getLong(com.tachyonmusic.core.constants.MediaAction.EndTime))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                com.tachyonmusic.core.constants.MediaAction.SetEndTimeEvent,
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
                com.tachyonmusic.core.constants.MediaAction.RequestMediaSourceReloadEvent,
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

            onLoopsReceived(extras!!.getParcelableArrayList(com.tachyonmusic.core.constants.MediaAction.Loops)!!)
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                com.tachyonmusic.core.constants.MediaAction.SendLoopsEvent,
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

            onPlaylistsReceived(extras!!.getParcelableArrayList(com.tachyonmusic.core.constants.MediaAction.Playlists)!!)
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                com.tachyonmusic.core.constants.MediaAction.SendPlaylistsEvent,
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

            onCombinePlaybackTypesChanged(extras!!.getBoolean(com.tachyonmusic.core.constants.MediaAction.CombinePlaybackTypes))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                com.tachyonmusic.core.constants.MediaAction.CombinePlaybackTypesChangedEvent,
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
                com.tachyonmusic.core.constants.MediaAction.RequestPlaybackUpdateEvent,
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

            onCurrentPlaylistIndexChanged(extras!!.getInt(com.tachyonmusic.core.constants.MediaAction.CurrentPlaylistIndex))
        }

        override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? =
            PlaybackStateCompat.CustomAction.Builder(
                com.tachyonmusic.core.constants.MediaAction.CurrentPlaylistIndexChangedEvent,
                javaClass.name,
                R.drawable.music_note
            ).build()
    }
}


