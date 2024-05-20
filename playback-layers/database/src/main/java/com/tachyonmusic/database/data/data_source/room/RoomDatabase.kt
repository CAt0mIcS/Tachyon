package com.tachyonmusic.database.data.data_source.room

import androidx.room.TypeConverters
import com.tachyonmusic.database.data.data_source.*
import com.tachyonmusic.database.domain.Converters
import com.tachyonmusic.database.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

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


    @Suppress("JSON_FORMAT_REDUNDANT") // Won't be called often, Json {} creation not slow
    override suspend fun toJson() = Json {
        prettyPrint = true
    }.encodeToString(
        buildJsonObject {
            put("Settings", Json.encodeToJsonElement(settingsDao.getSettings()))
            put("Songs", Json.encodeToJsonElement(songDao.getSongs()))
            put("CustomizedSongs", Json.encodeToJsonElement(customizedSongDao.getCustomizedSongs()))
            put("Playlists", Json.encodeToJsonElement(playlistDao.getPlaylists()))
            put("History", Json.encodeToJsonElement(historyDao.getHistory()))
            put("Data", Json.encodeToJsonElement(dataDao.getData()))
        }
    )

    override suspend fun overrideFromJson(json: String) {
        clearAllTables()

        val obj = Json.decodeFromString<JsonObject>(json)
        val settings =
            Json.decodeFromJsonElement<SettingsEntity>(obj["Settings"] ?: buildJsonObject { })
        val songs = Json.decodeFromJsonElement<List<SongEntity>>(obj["Songs"]!!)
        val customizedSongs =
            Json.decodeFromJsonElement<List<CustomizedSongEntity>>(obj["CustomizedSongs"]!!)
        val playlists = Json.decodeFromJsonElement<List<PlaylistEntity>>(obj["Playlists"]!!)
        val history = Json.decodeFromJsonElement<List<HistoryEntity>>(obj["History"]!!)
        val data = Json.decodeFromJsonElement<DataEntity>(obj["Data"] ?: buildJsonObject { })

        settingsDao.setSettings(settings)
        songDao.addAll(songs)
        customizedSongDao.addAll(customizedSongs)
        playlistDao.addAll(playlists)
        historyDao.addAll(history)
        dataDao.setData(data)
    }

    override fun checkpoint() {
        val db = openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL);", emptyArray())
        db.query("PRAGMA wal_checkpoint(TRUNCATE);", emptyArray())
    }

    override val readableDatabasePath: String
        get() = openHelper.readableDatabase.path!!
}