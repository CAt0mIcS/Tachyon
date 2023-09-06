package com.tachyonmusic.domain.use_case.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tachyonmusic.database.data.data_source.Database
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

class ImportDatabase(
    private val database: Database,
    private val context: Context
) {
    operator fun invoke(source: Uri?, restart: Boolean = true) {
        if (source == null)
            return

        val dbFile = File(database.readableDatabasePath)
        val bkpFile = File(dbFile.path + "-bkp.zip")
        try {
            if (bkpFile.exists()) bkpFile.delete()

            val outputStream = BufferedOutputStream(FileOutputStream(bkpFile))
            val inputStream = context.contentResolver.openInputStream(source) ?: return
            inputStream.copyTo(outputStream)
            inputStream.close()

            unzip(bkpFile, dbFile.parent ?: return)
            outputStream.close()
            database.checkpoint()

            if (bkpFile.exists()) bkpFile.delete()

            // TODO: Move somewhere else
            if (restart) {
                val i = context.packageManager.getLaunchIntentForPackage(context.packageName)
                i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(i)
                System.exit(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun unzip(source: File, outputDirectory: String) {
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
    }
}