package com.daton.mucify.user

import android.content.Context
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.io.*

@Serializable
data class UserMetadata(
    @Transient
    private val settingsFile: File = File("data/Settings.txt"),

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
    var maxPlaybacksInHistory: Int = 25
) {

    /**
     * Loads the settings from the local predefined settings file. If the file doesn't exist
     * it's created and default settings will be saved in it
     */
    fun loadFromLocal() {
        // If reading fails, save default settings
        try {
            val jsonBuilder = StringBuilder()
            val reader = BufferedReader(FileReader(settingsFile))
            while (reader.ready()) {
                jsonBuilder.append(reader.readLine()).append('\n')
            }
            reader.close()

            loadFromString(jsonBuilder.toString())
        } catch (e: IOException) {
            saveToLocal()
        } catch (e: JSONException) {
            saveToLocal()
        }
    }

    /**
     * Saves the current settings to the predefined local settings file. Saves nothing in case of failure
     */
    fun saveToLocal() {
        try {
            val writer = BufferedWriter(FileWriter(settingsFile))
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

}