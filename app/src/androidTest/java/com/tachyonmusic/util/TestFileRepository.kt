package com.tachyonmusic.util

import android.os.Environment
import com.tachyonmusic.domain.repository.FileRepository
import java.io.File

internal class TestFileRepository : FileRepository {
    override fun getFilesInDirectoryWithExtensions(
        directory: File,
        extensions: List<String>
    ): List<File> = List(100) {
        File(Environment.getExternalStorageDirectory().absolutePath + "/Music/TestFile$it.mp3")
    }
}