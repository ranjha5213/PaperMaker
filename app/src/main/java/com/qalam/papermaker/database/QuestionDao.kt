package com.qalam.papermaker.database
import androidx.room.*
import com.qalam.papermaker.models.Question
@Dao
interface QuestionDao { @Query("SELECT * FROM Question") fun getAll(): List<Question> }
