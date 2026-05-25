package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_times")
data class PrayerTime(
    @PrimaryKey val id: String, // "fajr", "dhuhr", "asr", "maghrib", "isha", "jumuah"
    val englishName: String,
    val bengaliName: String,
    val time: String,
    val iqamahTime: String = ""
)
