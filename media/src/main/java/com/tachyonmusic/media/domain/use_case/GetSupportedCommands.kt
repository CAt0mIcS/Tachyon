package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import com.tachyonmusic.core.constants.MediaAction


// TODO: Sort out which commands to enable/disable
class GetSupportedCommands {
    operator fun invoke(): MediaSession.ConnectionResult {
        val sessionCommands = SessionCommands.Builder().apply {
            add(SessionCommand.COMMAND_CODE_LIBRARY_GET_LIBRARY_ROOT)
            add(SessionCommand.COMMAND_CODE_LIBRARY_SUBSCRIBE)
            add(SessionCommand.COMMAND_CODE_LIBRARY_UNSUBSCRIBE)
            add(SessionCommand.COMMAND_CODE_LIBRARY_GET_CHILDREN)
            add(SessionCommand.COMMAND_CODE_LIBRARY_GET_ITEM)
            add(SessionCommand.COMMAND_CODE_LIBRARY_SEARCH)
            add(SessionCommand.COMMAND_CODE_LIBRARY_GET_SEARCH_RESULT)

            ////////////////////////////////////////////////////////////////////////////////////////
            // CustomCommands
            add(MediaAction.setPlaybackCommand)
            add(MediaAction.updateTimingDataCommand)
        }.build()

        val playerCommands = Player.Commands.Builder().apply {
            add(Player.COMMAND_PLAY_PAUSE)
            add(Player.COMMAND_PREPARE)
            add(Player.COMMAND_STOP)
            add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
            add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            add(Player.COMMAND_SEEK_TO_PREVIOUS)
            add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            add(Player.COMMAND_SEEK_TO_NEXT)
            add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            add(Player.COMMAND_SEEK_BACK)
            add(Player.COMMAND_SEEK_FORWARD)
            add(Player.COMMAND_SET_SPEED_AND_PITCH)
            add(Player.COMMAND_SET_SHUFFLE_MODE)
            add(Player.COMMAND_SET_REPEAT_MODE)
            add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
            add(Player.COMMAND_GET_TIMELINE)
            add(Player.COMMAND_GET_MEDIA_ITEMS_METADATA)
            add(Player.COMMAND_SET_MEDIA_ITEMS_METADATA)
            add(Player.COMMAND_SET_MEDIA_ITEM)
            add(Player.COMMAND_CHANGE_MEDIA_ITEMS)
            add(Player.COMMAND_GET_AUDIO_ATTRIBUTES)
            add(Player.COMMAND_GET_VOLUME)
            add(Player.COMMAND_GET_DEVICE_VOLUME)
            add(Player.COMMAND_SET_VOLUME)
            add(Player.COMMAND_SET_DEVICE_VOLUME)
            add(Player.COMMAND_ADJUST_DEVICE_VOLUME)
            add(Player.COMMAND_SET_VIDEO_SURFACE)
            add(Player.COMMAND_GET_TEXT)
            add(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
            add(Player.COMMAND_GET_TRACKS)
        }.build()

        return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
    }
}