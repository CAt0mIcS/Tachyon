package com.tachyonmusic.database.data.data_source

interface Database {
    val settingsDao: SettingsDao
    val songDao: SongDao
    val loopDao: LoopDao
    val playlistDao: PlaylistDao
    val historyDao: HistoryDao
    val dataDao: DataDao

    fun clearAllTables()
}