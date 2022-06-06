package com.daton.media.ext

import com.daton.media.device.MediaSource
import java.io.File


inline val File.isSongFile: Boolean
    get() = isFile && MediaSource.SupportedAudioExtensions.contains(extension)

inline val File.isLoopFile: Boolean
    get() = isFile && extension == MediaSource.LoopFileExtension

inline val File.isPlaylistFile: Boolean
    get() = isFile && extension == MediaSource.PlaylistFileExtension

fun File.toSongMediaId(): String = "Song_$absolutePath"
fun File.toLoopMediaId(): String = "Loop_$absolutePath"
fun File.toPlaylistMediaId(): String = "Playlist_$absolutePath"