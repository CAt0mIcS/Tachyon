package com.tachyonmusic.playback_layers

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity

fun Uri?.isPlayable(context: Context) =
    if (this == null) false else DocumentFile.fromTreeUri(context, this)!!.canRead()

fun SinglePlaybackEntity.checkIfPlayable(context: Context) =
    mediaId.uri?.isPlayable(context) == true