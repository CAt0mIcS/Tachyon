package com.tachyonmusic.user.data

import android.content.Context
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.UiText
import com.tachyonmusic.user.R
import org.json.JSONException
import java.io.*

class LocalCache(context: Context) {

    private val cache = File(context.filesDir.absolutePath.toString() + "/Cache.txt")

    fun get() = loadFromLocal()

    fun set(metadata: Metadata) = saveToLocal(metadata)

    /**
     * Loads the settings from the local predefined settings file. If the file doesn't exist
     * it's created and default settings will be saved in it
     *
     * @return New metadata
     */
    private fun loadFromLocal(): Metadata {
        // If reading fails, save default settings
        try {
            val jsonBuilder = StringBuilder()
            val reader = BufferedReader(FileReader(cache))
            while (reader.ready()) {
                jsonBuilder.append(reader.readLine()).append('\n')
            }
            reader.close()

            return Metadata(jsonBuilder.toString())
        } catch (e: IOException) {
            saveToLocal(Metadata())
        } catch (e: JSONException) {
            saveToLocal(Metadata())
        }
        return Metadata()
    }

    /**
     * Saves the current metadata to the predefined local settings file. Saves nothing in case of failure.
     * Updates the timestamp to the current system time
     */
    private fun saveToLocal(metadata: Metadata): Resource<Unit> =
        try {
            val writer = BufferedWriter(FileWriter(cache))
            writer.write(metadata.toString())
            writer.close()
            Resource.Success()
        } catch (e: Exception) {
            Resource.Error(
                if (e.localizedMessage != null) UiText.DynamicString(e.localizedMessage!!)
                else UiText.StringResource(R.string.unknown_error)
            )
        }
}