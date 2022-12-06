package com.tachyonmusic.data.repository

import com.tachyonmusic.domain.repository.FileRepository
import java.io.File

class FileRepositoryImpl : FileRepository {

    // TODO: This should be a data_source?
    override fun getFilesInDirectoryWithExtensions(
        directory: File,
        extensions: List<String>
    ): List<File> {
        val files = directory.listFiles() ?: arrayOf()
        val returnFiles = mutableListOf<File>()
        for (file in files) {
            if (extensions.contains(file.extension)) {
                returnFiles += file
            }
        }
        return returnFiles
    }
}