package com.tachyonmusic.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

fun Uri?.isPlayable(context: Context) =
    if (this == null) false else DocumentFile.fromTreeUri(context, this)!!.canRead()