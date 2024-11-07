package com.tachyonmusic.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.util.File
import com.tachyonmusic.util.extension
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import kotlin.math.ceil

class FileRepositoryImpl(
    private val context: Context
) : FileRepository {

    // TODO: This should be a data_source?
    override fun getFilesInDirectoriesWithExtensions(
        directories: List<Uri>,
        extensions: List<String>
    ): List<DocumentFile> {
        val files = mutableListOf<DocumentFile>()

        for (directory in directories) {
            runBlocking {
                files += DocumentFile.fromTreeUri(context, directory)!!.recurseFiles(extensions)
            }
        }

        return files
    }


    private suspend fun DocumentFile.recurseFiles(extensions: List<String>): List<DocumentFile> =
        withContext(Dispatchers.IO) {
            val readableFiles = mutableListOf<Deferred<List<DocumentFile>>>()
            val filesInDirectory = listFiles().toList()
            val chunkSize =
                if (filesInDirectory.size < 50) filesInDirectory.size else ceil(filesInDirectory.size * .05f).toInt()

            for (chunk in filesInDirectory.chunked(chunkSize)) {
                readableFiles += async {
                    val newFiles = mutableListOf<DocumentFile>()
                    for (file in chunk) {
                        if (file.isDirectory)
                            newFiles += file.recurseFiles(extensions)
                        else if (file.canRead() && file.name != null &&
                            extensions.contains(File(file.name!!).extension)
                        )
                            newFiles += file
                    }

                    newFiles
                }
            }

            readableFiles.awaitAll().flatten()
        }
}