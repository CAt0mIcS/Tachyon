package com.daton.database.data.data_source.firestore

import com.daton.database.data.data_source.DataDao
import com.daton.database.data.data_source.Database
import com.daton.database.data.data_source.HistoryDao
import com.daton.database.data.data_source.LoopDao
import com.daton.database.data.data_source.PlaylistDao
import com.daton.database.data.data_source.SettingsDao
import com.daton.database.data.data_source.SongDao


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
}