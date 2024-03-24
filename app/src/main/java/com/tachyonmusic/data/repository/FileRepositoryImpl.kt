package com.tachyonmusic.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.util.File
import com.tachyonmusic.util.extension

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
            files += DocumentFile.fromTreeUri(context, directory)!!.recurseFiles(extensions)
        }

        return files
    }


    private fun DocumentFile.recurseFiles(extensions: List<String>): List<DocumentFile> {
        val files = mutableListOf<DocumentFile>()
        for (file in listFiles()) {
            if (file.isDirectory)
                files += file.recurseFiles(extensions)
            else if (file.canRead() && file.name != null && extensions.contains(File(file.name!!).extension))
                files += file
        }
        return files
    }
}