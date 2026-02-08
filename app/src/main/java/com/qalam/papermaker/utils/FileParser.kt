package com.qalam.papermaker.utils

import com.qalam.papermaker.models.Question
import java.io.BufferedReader
import java.io.StringReader
import java.util.regex.Pattern

class FileParser {
    companion object {
        private val QUESTION_PATTERN = Pattern.compile("^(\d+)[\.\)]\s*(.+)$")
        private val OPTION_PATTERN = Pattern.compile("^([ا-یa-dA-D])[\.\)]\s*(.+)$", Pattern.CASE_INSENSITIVE)
    }

    fun parseText(text: String): List<Question> {
        return when {
            text.contains(",,") || text.split("\n").firstOrNull()?.contains(",") == true -> {
                parseCsv(text)
            }
            else -> {
                parsePlainText(text)
            }
        }
    }

    fun parsePlainText(text: String): List<Question> {
        val questions = mutableListOf<Question>()
        val reader = BufferedReader(StringReader(text))
        var currentQuestion: Question? = null
        var optionCount = 0

        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) {
                    if (currentQuestion != null && optionCount == 4) {
                        questions.add(currentQuestion!!)
                        currentQuestion = null
                        optionCount = 0
                    }
                    return@forEach
                }

                val questionMatcher = QUESTION_PATTERN.matcher(trimmedLine)
                if (questionMatcher.find()) {
                    if (currentQuestion != null && optionCount == 4) {
                        questions.add(currentQuestion!!)
                    }
                    val questionText = questionMatcher.group(2)?.trim() ?: ""
                    currentQuestion = Question(
                        id = questions.size.toLong(),
                        questionText = questionText,
                        optionA = "", optionB = "", optionC = "", optionD = ""
                    )
                    optionCount = 0
                    return@forEach
                }

                val optionMatcher = OPTION_PATTERN.matcher(trimmedLine)
                if (optionMatcher.find() && currentQuestion != null) {
                    val optionLetter = optionMatcher.group(1)?.lowercase()
                    val optionText = optionMatcher.group(2)?.trim() ?: ""
                    when (optionLetter) {
                        "الف", "ا", "a" -> { currentQuestion = currentQuestion!!.copy(optionA = optionText); optionCount++ }
                        "ب", "b" -> { currentQuestion = currentQuestion!!.copy(optionB = optionText); optionCount++ }
                        "ج", "c" -> { currentQuestion = currentQuestion!!.copy(optionC = optionText); optionCount++ }
                        "د", "d" -> { currentQuestion = currentQuestion!!.copy(optionD = optionText); optionCount++ }
                    }
                }
            }
        }
        if (currentQuestion != null && optionCount == 4) questions.add(currentQuestion!!)
        return questions
    }

    fun parseCsv(csvText: String): List<Question> {
        val questions = mutableListOf<Question>()
        val lines = csvText.trim().split("\n")
        var isFirstLine = true
        var idCounter = 0
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            if (isFirstLine && (trimmedLine.contains("سوال") || trimmedLine.contains("question", true))) {
                isFirstLine = false
                continue
            }
            val columns = parseCsvLine(trimmedLine)
            if (columns.size >= 5) {
                questions.add(Question(
                    id = idCounter++.toLong(),
                    questionText = columns[0].trim(),
                    optionA = columns[1].trim(),
                    optionB = columns[2].trim(),
                    optionC = columns[3].trim(),
                    optionD = columns[4].trim()
                ))
            }
        }
        return questions
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"'); i++
                } else inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString()); current = StringBuilder()
            } else current.append(c)
            i++
        }
        result.add(current.toString())
        return result
    }

    fun detectFormat(text: String): String {
        return when {
            text.contains(",,") || text.split("\n").firstOrNull()?.contains(",") == true -> "CSV"
            QUESTION_PATTERN.matcher(text).find() -> "TEXT"
            else -> "UNKNOWN"
        }
    }

    fun questionsToCsv(questions: List<Question>): String {
        val csv = StringBuilder()
        csv.append("سوال,اختیار الف,اختیار ب,اختیار ج,اختیار د\n")
        questions.forEach { csv.append(it.toCsvRow()).append("\n") }
        return csv.toString()
    }

    fun questionsToText(questions: List<Question>): String {
        val text = StringBuilder()
        questions.forEachIndexed { index, q ->
            text.append("${index + 1}. ${q.questionText}\nالف) ${q.optionA}\nب) ${q.optionB}\nج) ${q.optionC}\nد) ${q.optionD}\n\n")
        }
        return text.toString()
    }
}
