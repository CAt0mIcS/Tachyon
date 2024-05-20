package com.tachyonmusic.database.data.data_source

interface Database {
    val settingsDao: SettingsDao
    val songDao: SongDao
    val customizedSongDao: CustomizedSongDao
    val playlistDao: PlaylistDao
    val historyDao: HistoryDao
    val dataDao: DataDao

    suspend fun toJson(): String
    suspend fun overrideFromJson(json: String)

    fun checkpoint()
    val readableDatabasePath: String

    companion object {
        const val NAME = "TachyonDatabase"

        const val JSON_MIME_TYPE = "application/json"
        const val BACKUP_FILE_NAME = NAME + "Backup.json"
    }
}