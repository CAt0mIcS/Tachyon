package com.tachyonmusic.domain.repository

import com.tachyonmusic.util.File


interface FileRepository {
    /**
     * @param extensions without the dot
     */
    fun getFilesInDirectoryWithExtensions(directory: File, extensions: List<String>): List<File>
}