package com.tachyonmusic.user.data

import android.content.Context
import com.google.gson.Gson
import com.tachyonmusic.user.R
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import org.json.JSONException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class LocalCache(
    context: Context,
    private val gson: Gson,
    uid: String? = null
) : UserRepository.EventListener {

    private val filesDir = context.filesDir.absolutePath

    val cache = File("$filesDir/Cache.txt")
    var uidFile = if (uid != null) File("$filesDir/$uid") else null
        private set

    init {
        if (!cache.exists() && uid == null)
            saveToLocal(Metadata(gson))
    }

    fun get() = loadFromLocal()

    fun set(metadata: Metadata) = saveToLocal(metadata)

    /**
     * LocalCache only exists if the user has never signed in before. [cache] will be deleted once
     * the user signs in.
     */
    val exists: Boolean
        get() = cache.exists() && uidFile == null

    override fun onUserChanged(uid: String?) {
        if (uid != null) {
            /**
             * User signed in, delete [cache] and create [uid] file
             */
            uidFile = File("$filesDir/$uid")
            uidFile!!.createNewFile()
            cache.delete()
        } else {
            /**
             * User was removed, remove [uid] file, but don't recreate [cache] as firebase will handle
             * this for us
             */
            uidFile?.delete()
        }
    }

    fun reset() {
        if (uidFile == null)
            saveToLocal(Metadata(gson))
        else
            cache.delete()
    }

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

            return Metadata(gson, jsonBuilder.toString())
        } catch (e: IOException) {
            saveToLocal(Metadata(gson))
        } catch (e: JSONException) {
            saveToLocal(Metadata(gson))
        }
        return Metadata(gson)
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