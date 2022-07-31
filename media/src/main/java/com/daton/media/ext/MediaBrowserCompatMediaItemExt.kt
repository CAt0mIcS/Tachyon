package com.daton.media.ext

import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.daton.media.data.MediaId
import com.daton.media.data.MetadataKeys
import com.daton.media.device.Loop
import com.daton.media.device.Playlist
import com.daton.media.device.Song
import java.io.File

inline val MediaBrowserCompat.MediaItem.artist: String?
    get() = description.artist

inline val MediaBrowserCompat.MediaItem.title: String?
    get() = description.title as String?

inline val MediaBrowserCompat.MediaItem.duration: Long
    get() = description.duration

inline val MediaBrowserCompat.MediaItem.path: File
    get() = description.path

inline val MediaBrowserCompat.MediaItem.startTime: Long
    get() = description.startTime

inline val MediaBrowserCompat.MediaItem.endTime: Long
    get() = description.endTime

inline val MediaBrowserCompat.MediaItem.albumArt: Bitmap?
    get() = description.albumArt

inline val MediaBrowserCompat.MediaItem.playlistPlaybacks: List<MediaId>
    get() {
        assert(isPlaylist) { "Trying to get playlist playbacks from a media item that is not a playlist" }
        return description.playlistPlaybacks
    }

inline val MediaBrowserCompat.MediaItem.playlistName: String
    get() {
        assert(isPlaylist) { "Trying to get playlist name from a media item that is not a playlist" }
        return description.playlistName
    }

inline val MediaBrowserCompat.MediaItem.loopName: String
    get() {
        assert(isLoop) { "Trying to get loop name from a media item that is not a loop" }
        return description.loopName
    }

inline val MediaBrowserCompat.MediaItem.currentPlaylistPlaybackIndex: Int
    get() = description.currentPlaylistPlaybackIndex

inline val MediaBrowserCompat.MediaItem.isSong: Boolean
    get() = description.isSong

inline val MediaBrowserCompat.MediaItem.isLoop: Boolean
    get() = description.isLoop

inline val MediaBrowserCompat.MediaItem.isPlaylist: Boolean
    get() = description.isPlaylist


fun MediaBrowserCompat.MediaItem.toSong(): Song {
    assert(isSong) { "Trying to call toSong on a MediaItem that is not a song" }
    return Song(mediaId!!.toMediaId(), title!!, artist!!, duration, albumArt)
}

fun MediaBrowserCompat.MediaItem.toLoop(): Loop {
    assert(isLoop) { "Trying to call toLoop on a MediaItem that is not a loop" }
    return Loop(mediaId!!.toMediaId(), startTime, endTime)
}

fun MediaBrowserCompat.MediaItem.toPlaylist(): Playlist {
    assert(isPlaylist) { "Trying to call toPlaylist on a MediaItem that is not a playlist" }
    return Playlist(mediaId!!.toMediaId(), playlistPlaybacks, currentPlaylistPlaybackIndex)
}

