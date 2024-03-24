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
     * @return true if the database was imported successfully and if all the permissions to access
     * the imported songs already exist, false if it was imported successfully but the permissions don't
     * exist and null if there was an error
     */
    suspend operator fun invoke(source: Uri?, restart: Boolean = true) =
        withContext(Dispatchers.IO) {
            if (source == null)
                return@withContext null

            val dbFile = File(database.readableDatabasePath)
            val bkpFile = File(dbFile.path + "-bkp.zip")
            try {
                if (bkpFile.exists()) bkpFile.delete()

                val outputStream = BufferedOutputStream(FileOutputStream(bkpFile))
                val inputStream =
                    context.contentResolver.openInputStream(source) ?: return@withContext null
                inputStream.copyTo(outputStream)
                inputStream.close()

                val delete = !unzip(bkpFile, dbFile.parent ?: return@withContext null)
                outputStream.close()
                database.checkpoint()

                if (bkpFile.exists() && delete) bkpFile.delete()

                // Make sure we don't save music directories to which we don't have access to
                val settings = settingsRepository.getSettings()
                if (settings.musicDirectories.any { !permissionRepository.hasPermission(it) }) {
                    settingsRepository.update(musicDirectories = emptyList())
                    return@withContext false
                }
                return@withContext true

                // TODO: Move somewhere else; TODO: Required?
//            if (restart) {
//                val i = context.packageManager.getLaunchIntentForPackage(context.packageName)
//                i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                context.startActivity(i)
//                System.exit(0)
//            }
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