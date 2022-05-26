package com.daton.mucify.user

import android.content.Context
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.io.*

@Serializable
data class UserMetadata(
    /**
     * Start time since epoch when settings were last saved. Used to control which settings are new/old
     */
    var timestamp: Long = 0L,

    /**
     * Keep playing even if audio focus is lost
     */
    var ignoreAudioFocus: Boolean = false,

    /**
     * Interval by which the seekbars in the player should increment/decrement the time in milliseconds
     */
    var songIncDecInterval: Int = 100,

    /**
     * Interval by which the loop/song done check will be run
     */
    var audioUpdateInterval: Int = 100,

    /**
     * Max number of playbacks stored in the history
     */
    private var _maxPlaybacksInHistory: Int = 25,

    /**
     * Remotely stored loops.
     */
    val loops: MutableList<Loop> = mutableListOf(),

    /**
     * Remotely stored playlists. The string specifies the local file content
     */
    val playlists: MutableList<Playlist> = mutableListOf(),

    /**
     * History items with media id
     */
    private var _history: MutableList<String> = mutableListOf()
) {

    @Serializable
    data class Loop(

        /**
         * Loop's media id
         */
        var mediaId: String,
        var songMediaId: String,

        var startTime: Long,
        var endTime: Long
    )

    @Serializable
    data class Playlist(
        /**
         * Playlist's media id
         */
        var mediaId: String,

        /**
         * Specifies a list of all the media ids in the playlist
         */
        var mediaIds: MutableList<String>
    )

    @Transient
    var onHistoryChanged: (() -> Unit)? = null

    val history: List<String>
        get() = _history

    var maxPlaybacksInHistory: Int
        get() = _maxPlaybacksInHistory
        set(value) {
            _maxPlaybacksInHistory = value
            if (history.size > _maxPlaybacksInHistory) {
                shrinkHistory()
                onHistoryChanged?.invoke()
            }

        }

    fun addHistory(mediaId: String) {
        if (history.contains(mediaId)) {
            _history.remove(mediaId)
            _history.add(0, mediaId)
        } else {
            _history.add(0, mediaId)
            if (history.size > maxPlaybacksInHistory)
                shrinkHistory()
        }
        onHistoryChanged?.invoke()
    }

    fun clearHistory() {
        if (history.isNotEmpty()) {
            _history.clear()
            onHistoryChanged?.invoke()
        }
    }

    /**
     * Loads the settings from the local predefined settings file. If the file doesn't exist
     * it's created and default settings will be saved in it
     *
     * @return New metadata
     */
    fun loadFromLocal(): UserMetadata {
        // If reading fails, save default settings
        try {
            val jsonBuilder = StringBuilder()
            val reader = BufferedReader(FileReader(User.settingsFile))
            while (reader.ready()) {
                jsonBuilder.append(reader.readLine()).append('\n')
            }
            reader.close()

            return loadFromString(jsonBuilder.toString())
        } catch (e: IOException) {
            saveToLocal()
        } catch (e: JSONException) {
            saveToLocal()
        }
        return this
    }

    /**
     * Saves the current settings to the predefined local settings file. Saves nothing in case of failure.
     * Updates the timestamp to the current system time
     */
    fun saveToLocal() {
        timestamp = System.currentTimeMillis()
        try {
            val writer = BufferedWriter(FileWriter(User.settingsFile))
            writer.write(toJsonString(this))
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Loads settings from a json-readable string
         */
        fun loadFromString(jsonString: String): UserMetadata {
            return Json.decodeFromString(jsonString)
        }

        /**
         * @return settings encoded into json string
         */
        fun toJsonString(userMetadata: UserMetadata): String {
            return Json.encodeToString(userMetadata)
        }
    }

    /**
     * Shrinks length of [history] to [maxPlaybacksInHistory]
     */
    private fun shrinkHistory() {
        _history.subList(0, history.size - maxPlaybacksInHistory).clear()
    }

}