package com.example.urdupapermaker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.urdupapermaker.models.Paper
import com.example.urdupapermaker.models.Question

@Database(
    entities = [Question::class, Paper::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun paperDao(): PaperDao
}
