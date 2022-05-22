package com.daton.mucify

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object UserSettings {
    lateinit var settingsFile: File

    /**
     * Keep playing even if audio focus is lost
     */
    var ignoreAudioFocus = false

    /**
     * Interval by which the seekbars in the player should increment/decrement the time in milliseconds
     */
    var songIncDecInterval = 100

    /**
     * Interval by which the loop/song done check will be run
     */
    var audioUpdateInterval = 100

    /**
     * Max number of playbacks stored in the history
     */
    var maxPlaybacksInHistory = 25

    /**
     * Information about the last [maxPlaybacksInHistory] played items
     */
    val playbackInfos: ArrayList<PlaybackInfo> = ArrayList()

    class PlaybackInfo {
        /**
         * Path to the last file which was opened. Could be path to playlist, loop or song file
         */
        var playbackPath: File? = null

        /**
         * Is PlaybackPath is a playlist then this will be set to the last song/loop that was played
         * in the playlist
         */
        var lastPlayedPlaybackInPlaylist: File? = null

        /**
         * Position in milliseconds of the song or loop
         */
        var playbackPos = 0
        val isPlaylist: Boolean
            get() = lastPlayedPlaybackInPlaylist != null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as PlaybackInfo
            return playbackPath == that.playbackPath && Objects.equals(
                lastPlayedPlaybackInPlaylist,
                that.lastPlayedPlaybackInPlaylist
            )
        }
    }

    fun load(context: Context) {
        settingsFile = File(context.filesDir.absolutePath.toString() + "/Settings.txt")

        // If reading fails, save default settings
        val jsonString: String = try {
            val jsonBuilder = StringBuilder()
            val reader = BufferedReader(FileReader(settingsFile))
            while (reader.ready()) {
                jsonBuilder.append(reader.readLine()).append('\n')
            }
            reader.close()
            jsonBuilder.toString()
        } catch (e: IOException) {
            save()
            return
        }

        // If reading fails, save default settings
        try {
            val json = JSONObject(jsonString)
            ignoreAudioFocus = json.optBoolean("IgnoreAudioFocus", ignoreAudioFocus)
            songIncDecInterval = json.optInt("SongIncDecInterval", songIncDecInterval)
            audioUpdateInterval = json.optInt("AudioUpdateInterval", audioUpdateInterval)
            maxPlaybacksInHistory = json.optInt("MaxPlaybacksInHistory", maxPlaybacksInHistory)

            playbackInfos.clear()
            val jsonPlaybackInfos = json.getJSONArray("PlaybackInfos")
            for (i in 0 until jsonPlaybackInfos.length()) {
                val obj = jsonPlaybackInfos.getJSONObject(i)
                val info = PlaybackInfo()
                info.playbackPos = obj.optInt("PlaybackPos")
                if (obj.has("PlaybackPath")) info.playbackPath = File(obj.getString("PlaybackPath"))
                if (obj.has("LastPlayedPlaybackInPlaylist")) info.lastPlayedPlaybackInPlaylist =
                    File(obj.getString("LastPlayedPlaybackInPlaylist"))
                addPlaybackInfo(info)
            }

            // Remove oldest playbacks if we exceed max playbacks in history
            if (playbackInfos.size > maxPlaybacksInHistory) playbackInfos.subList(
                0,
                playbackInfos.size - maxPlaybacksInHistory
            ).clear()
        } catch (e: JSONException) {
            save()
            return
        }
    }

    fun save() {
        val map: MutableMap<String?, String?> = HashMap()
        map["IgnoreAudioFocus"] = ignoreAudioFocus.toString()
        map["SongIncDecInterval"] = songIncDecInterval.toString()
        map["AudioUpdateInterval"] = audioUpdateInterval.toString()
        map["MaxPlaybacksInHistory"] = maxPlaybacksInHistory.toString()
        val json = (map as Map<*, *>?)?.let { JSONObject(it) }
        val array = JSONArray()

        for (i in 0 until playbackInfos.size) {
            val obj = JSONObject()
            val info = playbackInfos[i]
            try {
                // Only store playback position for previously played playback
                if (i == playbackInfos.size - 1) obj.put("PlaybackPos", info.playbackPos)
                obj.putOpt("PlaybackPath", info.playbackPath)
                obj.putOpt("LastPlayedPlaybackInPlaylist", info.lastPlayedPlaybackInPlaylist)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            array.put(obj)
        }

        try {
            json!!.put("PlaybackInfos", array)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val writer = BufferedWriter(FileWriter(settingsFile))
            writer.write(json!!.toString(4))
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun addPlaybackInfo(info: PlaybackInfo) {
        for (i in 0 until playbackInfos.size) {
            if (info == playbackInfos[i]) {
                playbackInfos.removeAt(i)
                playbackInfos.add(info)
//                for (c in callbacks) c.onPlaybackInfoChanged()
                return
            }
        }
        playbackInfos.add(info)
//        for (c in callbacks) c.onPlaybackInfoChanged()
    }

    fun reset() {
        ignoreAudioFocus = false
        songIncDecInterval = 100
        audioUpdateInterval = 100
        maxPlaybacksInHistory = 25
        settingsFile.delete()
    }

}