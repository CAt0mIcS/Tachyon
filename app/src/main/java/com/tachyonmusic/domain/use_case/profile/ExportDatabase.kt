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

        val dbFile =
            context.getDatabasePath(Database.NAME)
        val dbWalFile = File(dbFile.path + Database.SQLITE_WALFILE_SUFFIX)
        val dbShmFile = File(dbFile.path + Database.SQLITE_SHMFILE_SUFFIX)
        val bkpFile = File(dbFile.path + "-bkp.zip")
        if (bkpFile.exists()) bkpFile.delete()
        database.checkpoint()
        try {
            zip(bkpFile.path, listOf(dbFile, dbWalFile, dbShmFile))
            val outputStream =
                context.contentResolver.openOutputStream(destination) ?: return@withContext
            outputStream.write(bkpFile.readBytes())
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun zip(outputPath: String, files: List<File>): Uri {
        val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(outputPath)))
        for (file in files) {
            if (!file.exists())
                continue

            val origin = BufferedInputStream(FileInputStream(file))
            val entry = ZipEntry(file.path.substring(file.path.lastIndexOf("/")))
            out.putNextEntry(entry)
            origin.copyTo(out)
            origin.close()
        }
        out.close()
        return Uri.fromFile(File(outputPath))
    }
}