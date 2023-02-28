package com.tachyonmusic.domain.repository

import android.net.Uri
import androidx.documentfile.provider.DocumentFile


interface FileRepository {
    /**
     * @param extensions without the dot
     */
    fun getFilesInDirectoriesWithExtensions(directories: List<Uri>, extensions: List<String>): List<DocumentFile>
}