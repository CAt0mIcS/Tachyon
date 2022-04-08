package com.example.mucify

import com.example.mucify.device.MediaSource
import java.io.File

object Util {
    fun isLoopFile(path: File): Boolean {
        return path.isFile && getFileExtension(path.name) == MediaSource.LoopFileExtension
    }

    fun isSongFile(path: File): Boolean {
        return path.isFile && MediaSource.SupportedAudioExtensions.contains(getFileExtension(path.name))
    }

    fun isPlaylistFile(path: File): Boolean {
        return path.isFile && getFileExtension(path.name) == MediaSource.PlaylistFileExtension
    }

    fun getFileExtension(filename: String): String {
        return if (!filename.contains(".")) "" else filename.substring(filename.lastIndexOf('.'))
    }
}