package com.qalam.papermaker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.qalam.papermaker.models.Paper
import com.qalam.papermaker.models.Question

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
