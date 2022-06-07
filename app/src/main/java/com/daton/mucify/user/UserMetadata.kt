package com.daton.mucify.user

import android.support.v4.media.MediaBrowserCompat
import com.daton.media.data.MediaId
import com.daton.media.device.Loop
import com.daton.media.device.Playlist
import com.daton.media.ext.endTime
import com.daton.media.ext.path
import com.daton.media.ext.startTime
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.io.*

@Serializable
class UserMetadata {
    /**
     * Start time since epoch when settings were last saved. Used to control which settings are new/old
     */
    var timestamp: Long = System.currentTimeMillis()
        private set

    /**
     * Keep playing even if audio focus is lost
     */
    var ignoreAudioFocus: Boolean = false
        set(value) {
            field = value
            timestamp = System.currentTimeMillis()
        }

    /**
     * Interval by which the seekbars in the player should increment/decrement the time in milliseconds
     */
    var songIncDecInterval: Int = 100
        set(value) {
            field = value
            timestamp = System.currentTimeMillis()
        }

    /**
     * Interval by which the loop/song done check will be run
     */
    var audioUpdateInterval: Int = 100
        set(value) {
            field = value
            timestamp = System.currentTimeMillis()
        }

    /**
     * Max number of playbacks stored in the history
     */
    var maxPlaybacksInHistory: Int = 25
        set(value) {
            field = value
            if (history.size > field) {
                shrinkHistory()
                onHistoryChanged?.invoke()
            }
            timestamp = System.currentTimeMillis()
        }

    /**
     * Specifies syncing information
     */
    var syncType: Int = SyncProvider.LOCAL_ONLY

    /**
     * Remotely stored loops.
     */
    val loops: MutableList<Loop> = mutableListOf()

    /**
     * Remotely stored playlists. The string specifies the local file content
     */
    val playlists: MutableList<Playlist> = mutableListOf()

    /**
     * History items with media id
     */
    val history: MutableList<MediaId> = mutableListOf()


    @Transient
    var onHistoryChanged: (() -> Unit)? = null

    operator fun plusAssign(loop: Loop) {
        loops.add(loop)
        timestamp = System.currentTimeMillis()
    }

    operator fun plusAssign(playlist: Playlist) {
        playlists.add(playlist)
        timestamp = System.currentTimeMillis()
    }

    operator fun plusAssign(history: MediaId) {
        addHistory(history)
    }


    fun addHistory(mediaId: MediaId) {
        if (history.contains(mediaId)) {
            history.remove(mediaId)
            history.add(0, mediaId)
        } else {
            history.add(0, mediaId)
            if (history.size > maxPlaybacksInHistory)
                shrinkHistory()
        }
        onHistoryChanged?.invoke()
        timestamp = System.currentTimeMillis()
    }

    fun clearHistory() {
        if (history.isNotEmpty()) {
            history.clear()
            onHistoryChanged?.invoke()
            timestamp = System.currentTimeMillis()
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
     * Saves the current metadata to the predefined local settings file. Saves nothing in case of failure.
     * Updates the timestamp to the current system time
     */
    fun saveToLocal() {
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
        history.subList(0, history.size - maxPlaybacksInHistory).clear()
    }
}