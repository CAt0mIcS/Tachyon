package com.tachyonmusic.domain.use_case.main

import android.app.Application
import android.content.Context
import com.tachyonmusic.core.SharedPrefsKeys

class IsFirstAppStart(
    private val app: Application
) {
    operator fun invoke(): Boolean {
        val prefs = app.getSharedPreferences(SharedPrefsKeys.NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(SharedPrefsKeys.FIRST_APP_START, true)
    }
}