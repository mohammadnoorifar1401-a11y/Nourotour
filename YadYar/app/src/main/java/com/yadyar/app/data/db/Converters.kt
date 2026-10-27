package com.yadyar.app.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromIntList(value: List<Int>?): String =
        value?.joinToString(",") ?: ""

    @TypeConverter
    fun toIntList(value: String?): List<Int> =
        value?.split(",")
            ?.mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
            ?: emptyList()
}
