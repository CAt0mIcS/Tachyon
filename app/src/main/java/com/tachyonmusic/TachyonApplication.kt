package com.tachyonmusic

import android.app.Activity
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class TachyonApplication : Application() {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    var mainActivity: Activity? = null // TODO: Remove
}