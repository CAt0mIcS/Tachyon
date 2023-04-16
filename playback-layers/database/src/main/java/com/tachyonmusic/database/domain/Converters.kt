package com.tachyonmusic.database.domain

import android.net.Uri
import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import java.lang.reflect.Type

object Converters {
    @TypeConverter
    fun fromStringToStringList(value: String?): List<String>? {
        val listType: Type = object : TypeToken<List<String?>?>() {}.type
        return if (value == null) null else Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringListToString(list: List<String>?): String? =
        if (list == null) null else Gson().toJson(list)


    @TypeConverter
    fun fromIntListToStringList(ints: List<Int>?) = ints?.map { it.toString() }

    @TypeConverter
    fun fromStringListToIntList(strings: List<String>?) = strings?.map { it.toInt() }


    @TypeConverter
    fun fromStringToMediaId(value: String?): MediaId? = MediaId.deserializeIfValid(value)

    @TypeConverter
    fun fromMediaIdToString(value: MediaId?): String? = value?.toString()


    @TypeConverter
    fun fromStringToMediaIdList(value: String?): List<MediaId> {
        val listType: Type = object : TypeToken<List<MediaId?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromMediaIdListToString(list: List<MediaId>?): String = Gson().toJson(list)


    @TypeConverter
    fun fromStringToTimingDataList(value: String?): List<TimingData> {
        val listType: Type = object : TypeToken<List<TimingData?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromTimingDataListToString(list: List<TimingData>?): String = Gson().toJson(list)


    @TypeConverter
    fun fromLongToDuration(value: Long?): Duration? = value?.ms

    @TypeConverter
    fun fromDurationToLong(value: Duration?): Long? = value?.inWholeMilliseconds


    @TypeConverter
    fun fromIntToRepeatMode(value: Int?) = if (value == null) null else RepeatMode.fromId(value)

    @TypeConverter
    fun fromRepeatModeToInt(value: RepeatMode?) = value?.id


    @TypeConverter
    fun fromListStringToListUri(value: List<String>?) = value?.map { Uri.parse(it) }

    @TypeConverter
    fun fromListUriToListString(value: List<Uri>?) = value?.map { it.toString() }
}