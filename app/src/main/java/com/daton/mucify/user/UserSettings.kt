package com.daton.mucify.user

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

    /**
     * Loads the settings from the local predefined settings file. If the file doesn't exist
     * it's created and default settings will be saved in it
     */
    fun loadFromLocal(context: Context) {
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
            saveToLocal()
            return
        }

        // If reading fails, save default settings
        try {
            loadFromString(jsonString)
        } catch (e: JSONException) {
            saveToLocal()
            return
        }
    }

    /**
     * Saves the current settings to the predefined local settings file. Saves nothing in case of failure
     */
    fun saveToLocal() {
        try {
            val writer = BufferedWriter(FileWriter(settingsFile))
            writer.write(toJsonString())
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * Loads settings from a json-readable string
     */
    fun loadFromString(jsonString: String) {
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

        // Remove oldest playbacks if we exceed max playbacks in history (TODO: Shouldn't be here)
        if (playbackInfos.size > maxPlaybacksInHistory) playbackInfos.subList(
            0,
            playbackInfos.size - maxPlaybacksInHistory
        ).clear()
    }

    /**
     * @return settings encoded into json string
     */
    fun toJsonString(): String {
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

        return json!!.toString(4)
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