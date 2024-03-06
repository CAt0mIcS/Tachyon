package com.tachyonmusic.core

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntRange

// https://registry.khronos.org/OpenSL-ES/specs/OpenSL_ES_Specification_1.0.1.pdf

data class ReverbConfig(
    @IntRange(ROOM_LEVEL_MIN, ROOM_LEVEL_MAX)
    var roomLevel: Short = ROOM_LEVEL_DEFAULT,

    @IntRange(ROOM_HF_LEVEL_MIN, ROOM_HF_LEVEL_MAX)
    var roomHFLevel: Short = ROOM_HF_LEVEL_DEFAULT,

    @IntRange(DECAY_TIME_MIN, DECAY_TIME_MAX)
    var decayTime: Int = DECAY_TIME_DEFAULT,

    @IntRange(DECAY_HF_RATIO_MIN, DECAY_HF_RATIO_MAX)
    var decayHFRatio: Short = DECAY_HF_RATIO_DEFAULT,

    @IntRange(REFLECTIONS_LEVEL_MIN, REFLECTIONS_LEVEL_MAX)
    var reflectionsLevel: Short = REFLECTIONS_LEVEL_DEFAULT,

    @IntRange(REFLECTIONS_DELAY_MIN, REFLECTIONS_DELAY_MAX)
    var reflectionsDelay: Int = REFLECTIONS_DELAY_DEFAULT,

    @IntRange(REVERB_LEVEL_MIN, REVERB_LEVEL_MAX)
    var reverbLevel: Short = REVERB_LEVEL_DEFAULT,

    @IntRange(REVERB_DELAY_MIN, REVERB_DELAY_MAX)
    var reverbDelay: Int = REVERB_DELAY_DEFAULT,

    @IntRange(DIFFUSION_MIN, DIFFUSION_MAX)
    var diffusion: Short = DIFFUSION_DEFAULT,

    @IntRange(DENSITY_MIN, DENSITY_MAX)
    var density: Short = DENSITY_DEFAULT
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt().toShort(),
        parcel.readInt().toShort(),
        parcel.readInt(),
        parcel.readInt().toShort(),
        parcel.readInt().toShort(),
        parcel.readInt(),
        parcel.readInt().toShort(),
        parcel.readInt(),
        parcel.readInt().toShort(),
        parcel.readInt().toShort()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(roomLevel.toInt())
        parcel.writeInt(roomHFLevel.toInt())
        parcel.writeInt(decayTime)
        parcel.writeInt(decayHFRatio.toInt())
        parcel.writeInt(reflectionsLevel.toInt())
        parcel.writeInt(reflectionsDelay)
        parcel.writeInt(reverbLevel.toInt())
        parcel.writeInt(reverbDelay)
        parcel.writeInt(diffusion.toInt())
        parcel.writeInt(density.toInt())
    }

    override fun describeContents() = 0

    companion object {
        const val MILLIBEL_MAX: Short = 0x7FFF
        const val MILLIBEL_MIN: Short = (-MILLIBEL_MAX - 1).toShort()

        val PRESET_GENERIC = ReverbConfig(-1000, -100, 1490, 830, -2602, 7, 200, 11, 1000, 1000)
        val PRESET_PADDEDCELL = ReverbConfig(-1000, -6000, 170, 100, -1204, 1, 207, 2, 1000, 1000)
        val PRESET_ROOM = ReverbConfig(-1000, -454, 400, 830, -1646, 2, 53, 3, 1000, 1000)
        val PRESET_BATHROOM = ReverbConfig(-1000, -1200, 1490, 540, -370, 7, 1030, 11, 1000, 600)
        val PRESET_LIVINGROOM = ReverbConfig(-1000, -6000, 500, 100, -1376, 3, -1104, 4, 1000, 1000)
        val PRESET_STONEROOM = ReverbConfig(-1000, -300, 2310, 640, -711, 12, 83, 17, 1000, 1000)
        val PRESET_AUDITORIUM = ReverbConfig(-1000, -476, 4320, 590, -789, 20, -289, 30, 1000, 1000)
        val PRESET_CONCERTHALL = ReverbConfig(-1000, -500, 3920, 700, -1230, 20, -2, 29, 1000, 1000)
        val PRESET_CAVE = ReverbConfig(-1000, 0, 2910, 1300, -602, 15, -302, 22, 1000, 1000)
        val PRESET_ARENA = ReverbConfig(-1000, -698, 7240, 330, -1166, 20, 16, 30, 1000, 1000)
        val PRESET_HANGAR = ReverbConfig(-1000, -1000, 10050, 230, -602, 20, 198, 30, 1000, 1000)
        val PRESET_CARPETEDHALLWAY =
            ReverbConfig(-1000, -4000, 300, 100, -1831, 2, -1630, 30, 1000, 1000)
        val PRESET_HALLWAY = ReverbConfig(-1000, -300, 1490, 590, -1219, 7, 441, 11, 1000, 1000)
        val PRESET_STONECORRIDOR =
            ReverbConfig(-1000, -237, 2700, 790, -1214, 13, 395, 20, 1000, 1000)
        val PRESET_ALLEY = ReverbConfig(-1000, -270, 1490, 860, -1204, 7, -4, 11, 1000, 1000)
        val PRESET_FOREST = ReverbConfig(-1000, -3300, 1490, 540, -2560, 162, -613, 88, 790, 1000)
        val PRESET_CITY = ReverbConfig(-1000, -800, 1490, 670, -2273, 7, -2217, 11, 500, 1000)
        val PRESET_MOUNTAINS =
            ReverbConfig(-1000, -2500, 1490, 210, -2780, 300, -2014, 100, 270, 1000)
        val PRESET_QUARRY =
            ReverbConfig(-1000, -1000, 1490, 830, MILLIBEL_MIN, 61, 500, 25, 1000, 1000)
        val PRESET_PLAIN = ReverbConfig(-1000, -2000, 1490, 500, -2466, 179, -2514, 100, 210, 1000)
        val PRESET_PARKINGLOT = ReverbConfig(-1000, 0, 1650, 1500, -1363, 8, -1153, 12, 1000, 1000)
        val PRESET_SEWERPIPE = ReverbConfig(-1000, -1000, 2810, 140, 429, 14, 648, 21, 800, 600)
        val PRESET_UNDERWATER = ReverbConfig(-1000, -4000, 1490, 100, -449, 7, 1700, 11, 1000, 1000)
        val PRESET_SMALLROOM = ReverbConfig(-1000, -600, 1100, 830, -400, 5, 500, 10, 1000, 1000)
        val PRESET_MEDIUMROOM =
            ReverbConfig(-1000, -600, 1300, 830, -1000, 20, -200, 20, 1000, 1000)
        val PRESET_LARGEROOM = ReverbConfig(-1000, -600, 1500, 830, -1600, 5, -1000, 40, 1000, 1000)
        val PRESET_MEDIUMHALL =
            ReverbConfig(-1000, -600, 1800, 700, -1300, 15, -800, 30, 1000, 1000)
        val PRESET_LARGEHALL =
            ReverbConfig(-1000, -600, 1800, 700, -2000, 30, -1400, 60, 1000, 1000)
        val PRESET_PLATE = ReverbConfig(-1000, -200, 1300, 900, 0, 2, 0, 10, 1000, 750)

        const val ROOM_LEVEL_MIN = MILLIBEL_MIN.toLong()
        const val ROOM_LEVEL_MAX = 0L
        val ROOM_LEVEL_DEFAULT = PRESET_GENERIC.roomLevel

        const val ROOM_HF_LEVEL_MIN = MILLIBEL_MIN.toLong()
        const val ROOM_HF_LEVEL_MAX = 0L
        val ROOM_HF_LEVEL_DEFAULT = PRESET_GENERIC.roomHFLevel

        const val DECAY_TIME_MIN = 100L
        const val DECAY_TIME_MAX = 20000L
        val DECAY_TIME_DEFAULT = PRESET_GENERIC.decayTime

        const val DECAY_HF_RATIO_MIN = 100L
        const val DECAY_HF_RATIO_MAX = 2000L
        val DECAY_HF_RATIO_DEFAULT = PRESET_GENERIC.decayHFRatio

        const val REFLECTIONS_LEVEL_MIN = MILLIBEL_MIN.toLong()
        const val REFLECTIONS_LEVEL_MAX = 1000L
        val REFLECTIONS_LEVEL_DEFAULT = PRESET_GENERIC.reflectionsLevel

        const val REFLECTIONS_DELAY_MIN = 0L
        const val REFLECTIONS_DELAY_MAX = 300L
        val REFLECTIONS_DELAY_DEFAULT = PRESET_GENERIC.reflectionsDelay

        const val REVERB_LEVEL_MIN = MILLIBEL_MIN.toLong()
        const val REVERB_LEVEL_MAX = 2000L
        val REVERB_LEVEL_DEFAULT = PRESET_GENERIC.reverbLevel

        const val REVERB_DELAY_MIN = 0L
        const val REVERB_DELAY_MAX = 100L
        val REVERB_DELAY_DEFAULT = PRESET_GENERIC.reverbDelay

        const val DIFFUSION_MIN = 0L
        const val DIFFUSION_MAX = 1000L
        val DIFFUSION_DEFAULT = PRESET_GENERIC.diffusion

        const val DENSITY_MIN = 0L
        const val DENSITY_MAX = 1000L
        val DENSITY_DEFAULT = PRESET_GENERIC.density

        @JvmField
        val CREATOR = object : Parcelable.Creator<ReverbConfig> {
            override fun createFromParcel(parcel: Parcel): ReverbConfig {
                return ReverbConfig(parcel)
            }

            override fun newArray(size: Int): Array<ReverbConfig?> {
                return arrayOfNulls(size)
            }
        }
    }
}