package com.tachyonmusic.permission

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import kotlinx.coroutines.flow.update

fun Uri?.isPlayable(context: Context) =
    if (this == null) false else DocumentFile.fromTreeUri(context, this)!!.canRead()

fun SinglePlaybackEntity.checkIfPlayable(context: Context) =
    mediaId.uri?.isPlayable(context) == true