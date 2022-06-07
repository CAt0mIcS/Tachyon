package com.daton.media.ext

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import com.daton.media.data.MediaId
import com.daton.media.data.MetadataKeys
import java.io.File


inline val MediaDescriptionCompat.artist: String
    get() = subtitle as String

inline val MediaDescriptionCompat.duration: Long
    get() = extras!!.getLong(MetadataKeys.Duration)

inline val MediaDescriptionCompat.path: File
    get() = File(extras!!.getString(MetadataKeys.MediaUri)!!)

inline val MediaDescriptionCompat.startTime: Long
    get() = extras!!.getLong(MetadataKeys.StartTime)

inline val MediaDescriptionCompat.endTime: Long
    get() = extras!!.getLong(MetadataKeys.EndTime)

inline val MediaDescriptionCompat.isSong: Boolean
    get() = mediaId!!.toMediaId().isSong

inline val MediaDescriptionCompat.isLoop: Boolean
    get() = mediaId!!.toMediaId().isLoop

inline val MediaDescriptionCompat.isPlaylist: Boolean
    get() = mediaId!!.toMediaId().isPlaylist


inline var MediaDescriptionCompat.Builder.title: String
    get() = throw IllegalAccessException("Cannot get from MediaDescriptionCompat.Builder")
    set(value) {
        setTitle(value)
    }

inline var MediaDescriptionCompat.Builder.artist: String
    get() = throw IllegalAccessException("Cannot get from MediaDescriptionCompat.Builder")
    set(value) {
        setSubtitle(value)
    }

inline var MediaDescriptionCompat.Builder.iconBitmap: Bitmap?
    get() = throw IllegalAccessException("Cannot get from MediaDescriptionCompat.Builder")
    set(value) {
        setIconBitmap(value)
    }

fun MediaDescriptionCompat.Builder.setExtras(
    path: File,
    duration: Long,
    startTime: Long,
    endTime: Long
) {
    val bundle = Bundle()
    bundle.putString(MetadataKeys.MediaUri, path.absolutePath)
    bundle.putLong(MetadataKeys.Duration, duration)
    bundle.putLong(MetadataKeys.StartTime, startTime)
    bundle.putLong(MetadataKeys.EndTime, endTime)
    setExtras(bundle)
}
