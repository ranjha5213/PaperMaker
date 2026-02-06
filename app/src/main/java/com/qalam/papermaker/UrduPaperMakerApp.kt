package com.qalam.papermaker

import android.app.Application
import androidx.room.Room
import com.qalam.papermaker.database.AppDatabase

class UrduPaperMakerApp : Application() {
    
    companion object {
        lateinit var instance: UrduPaperMakerApp
            private set
        
        lateinit var database: AppDatabase
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "urdu_paper_maker.db"
        ).fallbackToDestructiveMigration()
         .build()
        
        // Initialize preferences
        initializePreferences()
    }
    
    private fun initializePreferences() {
        // Setup default preferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean("first_run", true)
            putString("default_school", "")
            putString("default_class", "")
            putInt("default_question_count", 20)
            putBoolean("auto_save", true)
            putBoolean("dark_mode", false)
            apply()
        }
    }
}
