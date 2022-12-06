package com.tachyonmusic.domain.repository

import java.io.File

interface FileRepository {
    /**
     * @param extensions without the dot
     */
    fun getFilesInDirectoryWithExtensions(directory: File, extensions: List<String>): List<File>
}