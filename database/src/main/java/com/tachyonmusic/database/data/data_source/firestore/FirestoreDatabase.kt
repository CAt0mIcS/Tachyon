package com.tachyonmusic.database.data.data_source.firestore

import com.tachyonmusic.database.data.data_source.DataDao
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.data_source.HistoryDao
import com.tachyonmusic.database.data.data_source.LoopDao
import com.tachyonmusic.database.data.data_source.PlaylistDao
import com.tachyonmusic.database.data.data_source.SettingsDao
import com.tachyonmusic.database.data.data_source.SongDao


class FirestoreDatabase : Database {
    override val settingsDao: SettingsDao
        get() = TODO("Not yet implemented")

    override val songDao: SongDao = SongDaoImpl()

    override val loopDao: LoopDao
        get() = TODO("Not yet implemented")
    override val playlistDao: PlaylistDao
        get() = TODO("Not yet implemented")
    override val historyDao: HistoryDao
        get() = TODO("Not yet implemented")
    override val dataDao: DataDao
        get() = TODO("Not yet implemented")

    override fun clearAllTables() {
        TODO("Not yet implemented")
    }
}