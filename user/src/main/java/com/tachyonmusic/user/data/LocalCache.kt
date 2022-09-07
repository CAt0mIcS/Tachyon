package com.tachyonmusic.user.data

import android.content.Context
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.UiText
import com.tachyonmusic.user.R
import com.tachyonmusic.user.domain.UserRepository
import org.json.JSONException
import java.io.*

class LocalCache(context: Context, uid: String? = null) : UserRepository.EventListener {

    private val filesDir = context.filesDir.absolutePath

    private val cache = File("$filesDir/Cache.txt")
    private var uidFile =
        if (uid != null) File("$filesDir/$uid") else null

    init {
        if (!cache.exists() && uid == null)
            saveToLocal(Metadata())
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
            if (!uidFile!!.createNewFile())
                TODO("UID file creation unsuccessful")
            if (!cache.delete())
                TODO("Cache file deletion unsuccessful")
        } else {
            /**
             * User was removed, remove [uid] file, but don't recreate [cache] as firebase will handle
             * this for us
             */
            if (uidFile?.delete() == true)
                TODO("UID file deletion unsuccessful")
        }
    }

    fun reset() {
        if (uidFile == null)
            saveToLocal(Metadata())
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