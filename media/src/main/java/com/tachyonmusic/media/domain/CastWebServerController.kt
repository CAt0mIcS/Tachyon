package com.tachyonmusic.media.domain

import android.net.Uri

interface CastWebServerController {
    fun start(newItems: List<Uri>)
    fun stop()

    fun getUrl(uri: Uri): String
}