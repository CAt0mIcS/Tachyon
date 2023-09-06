package com.tachyonmusic.database.data.data_source.room

import androidx.room.TypeConverters
import com.tachyonmusic.database.data.data_source.*
import com.tachyonmusic.database.domain.Converters
import com.tachyonmusic.database.domain.model.*

@androidx.room.Database(
    entities = [
        SettingsEntity::class,
        SongEntity::class,
        CustomizedSongEntity::class,
        PlaylistEntity::class,
        PlaybackEntity::class,
        HistoryEntity::class,
        DataEntity::class
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class RoomDatabase : androidx.room.RoomDatabase(), Database {
    abstract override val settingsDao: SettingsDao
    abstract override val songDao: SongDao
    abstract override val customizedSongDao: CustomizedSongDao
    abstract override val playlistDao: PlaylistDao
    abstract override val historyDao: HistoryDao
    abstract override val dataDao: DataDao

    override fun clearAllTables() {
        TODO()
    }

    override fun checkpoint() {
        val db = openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL);", null)
        db.query("PRAGMA wal_checkpoint(TRUNCATE);", null)
    }

    override val readableDatabasePath: String
        get() = openHelper.readableDatabase.path
}