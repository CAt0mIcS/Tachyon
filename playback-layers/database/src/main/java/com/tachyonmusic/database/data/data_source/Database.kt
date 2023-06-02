package com.tachyonmusic.database.data.data_source

interface Database {
    val settingsDao: SettingsDao
    val songDao: SongDao
    val customizedSongDao: CustomizedSongDao
    val playlistDao: PlaylistDao
    val historyDao: HistoryDao
    val dataDao: DataDao

    fun clearAllTables()
}