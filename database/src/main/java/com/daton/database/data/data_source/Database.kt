package com.daton.database.data.data_source

import com.daton.database.data.data_source.DataDao
import com.daton.database.data.data_source.HistoryDao
import com.daton.database.data.data_source.LoopDao
import com.daton.database.data.data_source.PlaylistDao
import com.daton.database.data.data_source.SettingsDao
import com.daton.database.data.data_source.SongDao

interface Database {
    val settingsDao: SettingsDao
    val songDao: SongDao
    val loopDao: LoopDao
    val playlistDao: PlaylistDao
    val historyDao: HistoryDao
    val dataDao: DataDao
}