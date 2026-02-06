package com.example.urdupapermaker.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    
    val correctAnswer: Int = 0, // 0=A, 1=B, 2=C, 3=D
    val difficulty: Int = 1, // 1=easy, 2=medium, 3=hard
    val subject: String = "",
    val chapter: String = "",
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isSelected: Boolean = false,
    val isCustom: Boolean = false
) {
    companion object {
        const val OPTION_A = 0
        const val OPTION_B = 1
        const val OPTION_C = 2
        const val OPTION_D = 3
    }
    
    fun getOption(option: Int): String {
        return when (option) {
            OPTION_A -> optionA
            OPTION_B -> optionB
            OPTION_C -> optionC
            OPTION_D -> optionD
            else -> ""
        }
    }
    
    fun getAllOptions(): List<String> {
        return listOf(optionA, optionB, optionC, optionD)
    }
    
    fun isCorrect(answer: Int): Boolean {
        return correctAnswer == answer
    }
    
    fun toCsvRow(): String {
        return ""$questionText","$optionA","$optionB","$optionC","$optionD""
    }
    
   fun toTextFormat(): String {
        return "${id}. " + questionText + "\n" +
               "الف) " + optionA + "\n" +
               "ب) " + optionB + "\n" +
               "ج) " + optionC + "\n" +
               "د) " + optionD
    }

}
