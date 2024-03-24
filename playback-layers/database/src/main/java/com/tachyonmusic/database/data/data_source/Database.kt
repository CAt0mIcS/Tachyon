package com.tachyonmusic.database.data.data_source

interface Database {
    val settingsDao: SettingsDao
    val songDao: SongDao
    val customizedSongDao: CustomizedSongDao
    val playlistDao: PlaylistDao
    val historyDao: HistoryDao
    val dataDao: DataDao

    fun clearAllTables()

    fun checkpoint()
    val readableDatabasePath: String

    companion object {
        const val NAME = "TachyonDatabase"
        const val SQLITE_WALFILE_SUFFIX = "-wal"
        const val SQLITE_SHMFILE_SUFFIX = "-shm"

        const val ZIP_MIME_TYPE = "application/zip"
        const val BACKUP_FILE_NAME = NAME + "Backup.zip"
    }
}