package com.qalam.papermaker.database
import androidx.room.TypeConverter
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> = value?.split(",") ?: emptyList()
    @TypeConverter
    fun fromList(list: List<String>?): String = list?.joinToString(",") ?: ""
}
