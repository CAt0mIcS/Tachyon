package com.daton.media.ext

import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
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

//inline val MediaBrowserCompat.MediaItem.playlistPlaybacks: List<MediaId>
//    get() {
//        assert(isPlaylist) { "Trying to get playlist playbacks from a media item that is not a playlist" }
//        return description.playlistPlaybacks
//    }

//inline val MediaBrowserCompat.MediaItem.playlistName: String
//    get() {
//        assert(isPlaylist) { "Trying to get playlist name from a media item that is not a playlist" }
//        return description.playlistName
//    }

//inline val MediaBrowserCompat.MediaItem.loopName: String
//    get() {
//        assert(isLoop || (isPlaylist && mediaId!!.toMediaId().underlyingMediaId?.isLoop == true))
//        { "Trying to get loop name from a media item that is not a loop or isn't a playlist with a loop as underlyingMediaId" }
//        return description.loopName
//    }

//inline val MediaBrowserCompat.MediaItem.currentPlaylistPlaybackIndex: Int
//    get() = description.currentPlaylistPlaybackIndex
//
//inline val MediaBrowserCompat.MediaItem.isSong: Boolean
//    get() = description.isSong
//
//inline val MediaBrowserCompat.MediaItem.isLoop: Boolean
//    get() = description.isLoop
//
//inline val MediaBrowserCompat.MediaItem.isPlaylist: Boolean
//    get() = description.isPlaylist


//fun MediaBrowserCompat.MediaItem.toSong(): SongOld {
//    assert(isSong) { "Trying to call toSong on a MediaItem that is not a song" }
//    return SongOld(mediaId!!.toMediaId(), title!!, artist!!, duration, albumArt)
//}
//
//fun MediaBrowserCompat.MediaItem.toLoop(): LoopOldd {
//    assert(isLoop) { "Trying to call toLoop on a MediaItem that is not a loop" }
//    return LoopOldd(mediaId!!.toMediaId(), startTime, endTime)
//}
//
//fun MediaBrowserCompat.MediaItem.toPlaylist(): PlaylistOld {
//    assert(isPlaylist) { "Trying to call toPlaylist on a MediaItem that is not a playlist" }
//    return PlaylistOld(mediaId!!.toMediaId(), playlistPlaybacks, currentPlaylistPlaybackIndex)
//}

