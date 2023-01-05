package com.tachyonmusic.util

import android.os.Environment

internal val MUSIC_DIRECTORY = Environment.getExternalStorageDirectory().absolutePath + "/Music/"

internal fun getTestFiles(everyFile: (String) -> File) = List(100) {
    everyFile("${MUSIC_DIRECTORY}TestFile$it.mp3")
}
