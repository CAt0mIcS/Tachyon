package com.tachyonmusic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TachyonApplication : Application() {
    // TODO: Remove this and debugPrint function
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: TachyonApplication? = null
    }
}