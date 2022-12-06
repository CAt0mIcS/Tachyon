package com.daton.database.domain

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import java.lang.reflect.Type

object Converters {
    @TypeConverter
    fun fromStringToStringList(value: String?): List<String> {
        val listType: Type = object : TypeToken<List<String?>?>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringListToString(list: List<String>?): String = Gson().toJson(list)


    @TypeConverter
    fun fromStringToMediaId(value: String?): MediaId? = MediaId.deserializeIfValid(value)

    @TypeConverter
    fun fromMediaIdToString(value: MediaId?): String? = value?.toString()


    @TypeConverter
    fun fromStringToMediaIdList(value: String?): List<MediaId> {
        val listType: Type = object : TypeToken<List<MediaId?>?>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromMediaIdListToString(list: List<MediaId>?): String = Gson().toJson(list)


    @TypeConverter
    fun fromStringToTimingDataList(value: String?): List<TimingData> {
        val listType: Type = object : TypeToken<List<TimingData?>?>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromTimingDataListToString(list: List<TimingData>?): String = Gson().toJson(list)
}