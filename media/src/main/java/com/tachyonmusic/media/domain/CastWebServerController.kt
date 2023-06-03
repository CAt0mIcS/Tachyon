package com.tachyonmusic.media.domain

import android.net.Uri

interface CastWebServerController {
    fun start()
    fun stop()

    fun getUrl(uri: Uri): String
}