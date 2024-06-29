package com.tachyonmusic.domain.use_case.profile

import android.content.Context
import android.net.Uri
import com.tachyonmusic.database.data.data_source.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportDatabase(
    private val database: Database,
    private val context: Context
) {
    suspend operator fun invoke(destination: Uri?) = withContext(Dispatchers.IO) {
        if (destination == null)
            return@withContext

        database.checkpoint()
        try {
            val jsonString = database.toJson()
            val outputStream =
                context.contentResolver.openOutputStream(destination) ?: return@withContext
            outputStream.write(jsonString.encodeToByteArray())
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}