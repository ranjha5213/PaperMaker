package com.qalam.papermaker.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "papers")
data class Paper(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val schoolName: String,
    val className: String,
    val subject: String,
    val totalMarks: Int,
    val timeAllowed: Int, // in minutes
    
    val questionIds: List<Long>, // List of question IDs
    val questionCount: Int,
    
    val creationDate: Date = Date(),
    val lastModified: Date = Date(),
    val isTemplate: Boolean = false,
    val templateName: String = "",
    
    val instructions: String = "",
    val footerText: String = "اردو پرچہ میکر ایپ",
    
    // Paper settings
    val showAnswerKey: Boolean = false,
    val shuffleQuestions: Boolean = true,
    val shuffleOptions: Boolean = false,
    val showMarks: Boolean = true,
    val showPageNumbers: Boolean = true,
    
    // Formatting
    val fontSize: Int = 12,
    val lineSpacing: Float = 1.2f,
    val marginLeft: Float = 50f,
    val marginRight: Float = 50f,
    val marginTop: Float = 50f,
    val marginBottom: Float = 50f
) {
    fun getHeader(): String {
        return schoolName + "
" +
            $schoolName
            "کلاس: " + className + " - " + subject + "
" +
            "کل نمبر: " + totalMarks + " - وقت: " + timeAllowed + " منٹ"
        
    }
    
    fun getFileName(): String {
        val date = creationDate.time
        return "پرچہ_" + subject + "_" + date + ".pdf"
    }
}
