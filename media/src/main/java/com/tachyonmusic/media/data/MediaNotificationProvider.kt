package com.tachyonmusic.media.data

import android.content.Context
import androidx.media3.session.DefaultMediaNotificationProvider

class MediaNotificationProvider(val context: Context) : DefaultMediaNotificationProvider(context) {
//    override fun getMediaButtons(
//        session: MediaSession,
//        playerCommands: Player.Commands,
//        customLayout: ImmutableList<CommandButton>,
//        showPauseButton: Boolean
//    ): ImmutableList<CommandButton> {
//        // Skip to previous action.
//        val commandButtons = mutableListOf<CommandButton>()
//        if (playerCommands.containsAny(
//                Player.COMMAND_SEEK_TO_PREVIOUS,
//                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
//            )
//        ) {
//            val commandButtonExtras = Bundle()
//            commandButtonExtras.putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 0)
//            commandButtons.add(
//                CommandButton.Builder()
//                    .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
//                    .setIconResId(R.drawable.skip_previous)
//                    .setDisplayName(
//                        context.getString(R.string.seek_to_previous_description)
//                    )
//                    .setExtras(commandButtonExtras)
//                    .build()
//            )
//        }
//        if (playerCommands.contains(Player.COMMAND_PLAY_PAUSE)) {
//            val commandButtonExtras = Bundle()
//            commandButtonExtras.putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 1)
//            commandButtons.add(
//                CommandButton.Builder()
//                    .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
//                    .setIconResId(
//                        if (playWhenReady) R.drawable.pause else R.drawable.play
//                    )
//                    .setExtras(commandButtonExtras)
//                    .setDisplayName(
//                        if (playWhenReady) context.getString(R.string.pause_description) else context.getString(
//                            R.string.play_description
//                        )
//                    )
//                    .build()
//            )
//        }
//        // Skip to next action.
//        if (playerCommands.containsAny(
//                Player.COMMAND_SEEK_TO_NEXT,
//                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
//            )
//        ) {
//            val commandButtonExtras = Bundle()
//            commandButtonExtras.putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 2)
//            commandButtons.add(
//                CommandButton.Builder()
//                    .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
//                    .setIconResId(R.drawable.skip_next)
//                    .setExtras(commandButtonExtras)
//                    .setDisplayName(context.getString(R.string.seek_to_next_description))
//                    .build()
//            )
//        }
//        for (i in customLayout.indices) {
//            val button = customLayout[i]
//            if (button.sessionCommand != null
//                && button.sessionCommand!!.commandCode == SessionCommand.COMMAND_CODE_CUSTOM
//            ) {
//                commandButtons.add(button)
//            }
//        }
//        return commandButtons
//    }
}