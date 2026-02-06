package com.example.urdupapermaker.utils

import com.example.urdupapermaker.models.Question
import java.io.BufferedReader
import java.io.StringReader
import java.util.regex.Pattern

class FileParser {
    
    companion object {
        private val QUESTION_PATTERN = Pattern.compile("^(\d+)[\.\)]\s*(.+)$")
        private val OPTION_PATTERN = Pattern.compile("^([ا-یa-dA-D])[\.\)]\s*(.+)$", Pattern.CASE_INSENSITIVE)
    }
    
    /**
     * Parse text from various formats (TXT, CSV, etc.)
     */
    fun parseText(text: String): List<Question> {
        return when {
            text.contains(",,") || text.split("
").firstOrNull()?.contains(",") == true -> {
                parseCsv(text)
            }
            else -> {
                parsePlainText(text)
            }
        }
    }
    
    /**
     * Parse plain text with questions in format:
     * 1. سوال؟
     * الف) جواب الف
     * ب) جواب ب
     * ج) جواب ج
     * د) جواب د
     */
    fun parsePlainText(text: String): List<Question> {
        val questions = mutableListOf<Question>()
        val reader = BufferedReader(StringReader(text))
        
        var currentQuestion: Question? = null
        var lineNumber = 0
        var optionCount = 0
        
        reader.useLines { lines ->
            lines.forEach { line ->
                lineNumber++
                val trimmedLine = line.trim()
                
                if (trimmedLine.isEmpty()) {
                    // Save current question if we have all options
                    if (currentQuestion != null && optionCount == 4) {
                        questions.add(currentQuestion!!)
                        currentQuestion = null
                        optionCount = 0
                    }
                    return@forEach
                }
                
                // Check if line starts a new question
                val questionMatcher = QUESTION_PATTERN.matcher(trimmedLine)
                if (questionMatcher.find()) {
                    // Save previous question if exists
                    if (currentQuestion != null && optionCount == 4) {
                        questions.add(currentQuestion!!)
                    }
                    
                    val questionText = questionMatcher.group(2)?.trim() ?: ""
                    currentQuestion = Question(
                        id = questions.size.toLong(),
                        questionText = questionText,
                        optionA = "",
                        optionB = "",
                        optionC = "",
                        optionD = ""
                    )
                    optionCount = 0
                    return@forEach
                }
                
                // Check if line is an option
                val optionMatcher = OPTION_PATTERN.matcher(trimmedLine)
                if (optionMatcher.find() && currentQuestion != null) {
                    val optionLetter = optionMatcher.group(1)?.lowercase()
                    val optionText = optionMatcher.group(2)?.trim() ?: ""
                    
                    when (optionLetter) {
                        "الف", "ا", "a" -> {
                            currentQuestion = currentQuestion!!.copy(optionA = optionText)
                            optionCount++
                        }
                        "ب", "ب", "b" -> {
                            currentQuestion = currentQuestion!!.copy(optionB = optionText)
                            optionCount++
                        }
                        "ج", "ج", "c" -> {
                            currentQuestion = currentQuestion!!.copy(optionC = optionText)
                            optionCount++
                        }
                        "د", "د", "d" -> {
                            currentQuestion = currentQuestion!!.copy(optionD = optionText)
                            optionCount++
                        }
                    }
                }
            }
        }
        
        // Add last question if complete
        if (currentQuestion != null && optionCount == 4) {
            questions.add(currentQuestion!!)
        }
        
        return questions
    }
    
    /**
     * Parse CSV format:
     * سوال,اختیار الف,اختیار ب,اختیار ج,اختیار د
     */
    fun parseCsv(csvText: String): List<Question> {
        val questions = mutableListOf<Question>()
        val lines = csvText.trim().split("
")
        
        var isFirstLine = true
        var idCounter = 0
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            // Skip header if present
            if (isFirstLine && (trimmedLine.contains("سوال") || trimmedLine.contains("question", true))) {
                isFirstLine = false
                continue
            }
            
            // Parse CSV line
            val columns = parseCsvLine(trimmedLine)
            if (columns.size >= 5) {
                val question = Question(
                    id = idCounter++.toLong(),
                    questionText = columns[0].trim(),
                    optionA = columns.getOrElse(1) { "" }.trim(),
                    optionB = columns.getOrElse(2) { "" }.trim(),
                    optionC = columns.getOrElse(3) { "" }.trim(),
                    optionD = columns.getOrElse(4) { "" }.trim()
                )
                questions.add(question)
            }
        }
        
        return questions
    }
    
    /**
     * Parse a CSV line considering quoted values
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (i in line.indices) {
            val c = line[i]
            
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped quote
                        current.append('"')
                        i + 1 // Skip next char
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(c)
                }
            }
        }
        
        result.add(current.toString())
        return result
    }
    
    /**
     * Detect file format from content
     */
    fun detectFormat(text: String): String {
        return when {
            text.contains(",,") || text.split("
").firstOrNull()?.contains(",") == true -> "CSV"
            QUESTION_PATTERN.matcher(text).find() -> "TEXT"
            else -> "UNKNOWN"
        }
    }
    
    /**
     * Convert questions to CSV format
     */
    fun questionsToCsv(questions: List<Question>): String {
        val csv = StringBuilder()
        // Add header
        csv.append("سوال,اختیار الف,اختیار ب,اختیار ج,اختیار د\n")
        
        // Add questions
        questions.forEach { question ->
            csv.append(question.toCsvRow())
            csv.append("\n")
        }
        
        return csv.toString()
    }
    
    /**
     * Convert questions to text format
     */
    fun questionsToText(questions: List<Question>): String {
        val text = StringBuilder()
        
        questions.forEachIndexed { index, question ->
            text.append("${index + 1}. ${question.questionText}\n")
            text.append("الف) ${question.optionA}\n")
            text.append("ب) ${question.optionB}\n")
            text.append("ج) ${question.optionC}\n")
            text.append("د) ${question.optionD}\n")
            text.append("\n")
        }
        
        return text.toString()
    }
}
