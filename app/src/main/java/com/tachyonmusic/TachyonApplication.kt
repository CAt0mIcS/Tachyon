package com.tachyonmusic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class TachyonApplication : Application() {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // TODO: Remove this and debugPrint function
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: TachyonApplication? = null
    }
}