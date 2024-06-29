package com.tachyonmusic.domain.use_case.profile

import android.content.Context
import android.net.Uri
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

class ImportDatabase(
    private val database: Database,
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val permissionRepository: UriPermissionRepository
) {
    /**
     * @return empty list if the database was imported successfully and if all the permissions to access
     * the imported songs already exist, list with missing permission [Uri]s if it was imported successfully but the permissions don't
     * exist and null if there was an error
     */
    suspend operator fun invoke(source: Uri?) =
        withContext(Dispatchers.IO) {
            if (source == null)
                return@withContext null

            try {
                val inputStream =
                    context.contentResolver.openInputStream(source) ?: return@withContext null
                val jsonString = inputStream.readBytes().decodeToString()
                inputStream.close()

                database.checkpoint()
                database.overrideFromJson(jsonString)

                val settings = settingsRepository.getSettings()
                return@withContext settings.musicDirectories.filter {
                    !permissionRepository.hasPermission(it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            null
        }

    private fun unzip(source: File, outputDirectory: String): Boolean {
        return try {
            ZipFile(source).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        val filePath = outputDirectory + File.separator + entry.name
                        val output = BufferedOutputStream(FileOutputStream(filePath))
                        input.copyTo(output)
                        output.close()
                    }
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace() // source could be empty, don't copy it
            false
        }
    }
}