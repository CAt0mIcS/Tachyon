package com.tachyonmusic.domain.repository

import android.app.Activity
import android.content.Intent

interface SpotifyInterfacer {
    fun authorize(activity: Activity)
    fun onAuthorization(requestCode: Int, resultCode: Int, intent: Intent?)
}