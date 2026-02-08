package com.qalam.papermaker.database
import androidx.room.*
import com.qalam.papermaker.models.Paper
@Dao
interface PaperDao { @Query("SELECT * FROM Paper") fun getAll(): List<Paper> }
