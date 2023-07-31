package com.tachyonmusic.playback_layers

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.database.domain.model.SongEntity

fun Uri?.isPlayable(context: Context) =
    if (this == null) false else DocumentFile.fromTreeUri(context, this)!!.canRead()

/**
 * Checks if local file exists and if we have permission to read from that file.
 * If the receiver is a Spotify song null will be returned
 */
fun SongEntity.checkIfPlayable(context: Context): Boolean {
    assert(mediaId.isLocalSong) { "Can only check playability of local songs" }
    return mediaId.uri?.isPlayable(context) == true
}