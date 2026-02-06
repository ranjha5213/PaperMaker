package com.qalam.papermaker.pdf

import android.content.Context
import android.os.Environment
import com.qalam.papermaker.models.Question
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {
    
    companion object {
        private const val FONT_NAME = "fonts/NotoNastaliqUrdu-Regular.ttf"
    }
    
    private lateinit var urduFont: PdfFont
    
    suspend fun generatePaper(
        questions: List<Question>,
        schoolName: String,
        className: String,
        subject: String,
        totalMarks: Int = 100,
        fileName: String? = null
    ): File = withContext(Dispatchers.IO) {
        
        // Create output file
        val outputFile = getOutputFile(fileName ?: generateFileName())
        
        // Initialize PDF writer
        val writer = PdfWriter(FileOutputStream(outputFile))
        val pdf = PdfDocument(writer)
        val document = Document(pdf)
        
        try {
            // Load Urdu font
            urduFont = loadUrduFont()
            
            // Set RTL direction
            document.setRightToLeft()
            
            // Add header
            addHeader(document, schoolName, className, subject, totalMarks)
            
            // Add questions in two columns
            addQuestions(document, questions)
            
            // Add footer
            addFooter(document)
            
            document.close()
            
        } catch (e: Exception) {
            document.close()
            throw e
        }
        
        return@withContext outputFile
    }
    
    private fun loadUrduFont(): PdfFont {
        return try {
            val assetManager = context.assets
            val fontStream = assetManager.open(FONT_NAME)
            PdfFontFactory.createFont(fontStream.readBytes(), PdfEncodings.IDENTITY_H, true)
        } catch (e: Exception) {
            // Fallback to default font
            PdfFontFactory.createFont()
        }
    }
    
    private fun addHeader(
        document: Document,
        schoolName: String,
        className: String,
        subject: String,
        totalMarks: Int
    ) {
        // School name
        document.add(
            Paragraph(schoolName)
                .setFont(urduFont)
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f)
        )
        
        // Divider line
        document.add(
            Paragraph()
                .setMarginBottom(20f)
        )
        
        // Details in a table
        val detailsTable = Table(UnitValue.createPercentArray(floatArrayOf(33f, 33f, 34f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        detailsTable.addCell(
            Paragraph("کلاس: $className")
                .setFont(urduFont)
                .setFontSize(12f)
        )
        
        detailsTable.addCell(
            Paragraph("کل نمبر: $totalMarks")
                .setFont(urduFont)
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
        )
        
        detailsTable.addCell(
            Paragraph("نام طالب علم: ________________")
                .setFont(urduFont)
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        
        document.add(detailsTable)
        document.add(Paragraph().setMarginBottom(30f))
    }
    
    private fun addQuestions(document: Document, questions: List<Question>) {
        val twoColumnTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        questions.forEachIndexed { index, question ->
            val questionParagraph = Paragraph()
                .setFont(urduFont)
                .setFontSize(11f)
                .setMarginBottom(5f)
            
            // Question number and text
            questionParagraph.add("${index + 1}. ${question.questionText}\n")
            
            // Options in two rows
            questionParagraph.add("(الف) ${question.optionA}      (ب) ${question.optionB}\n")
            questionParagraph.add("(ج) ${question.optionC}      (د) ${question.optionD}")
            
            twoColumnTable.addCell(
                Paragraph(questionParagraph)
                    .setPadding(5f)
                    .setMarginBottom(15f)
            )
            
            // Add page break if needed (every 10 questions per column)
            if ((index + 1) % 10 == 0 && (index + 1) < questions.size) {
                document.add(twoColumnTable)
                document.add(Paragraph().setMarginBottom(30f))
                twoColumnTable.deleteBodyRows()
            }
        }
        
        // Add remaining questions
        if (twoColumnTable.numberOfRows > 0) {
            document.add(twoColumnTable)
        }
    }
    
    private fun addFooter(document: Document) {
        document.add(
            Paragraph()
                .setMarginTop(50f)
        )
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        document.add(
            Paragraph("تاریخ: $currentDate")
                .setFont(urduFont)
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.LEFT)
        )
        
        document.add(
            Paragraph("اردو پرچہ میکر ایپ")
                .setFont(urduFont)
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.RIGHT)
        )
    }
    
    private fun getOutputFile(fileName: String): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, "UrduPaperMaker")
        
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        
        return File(appDir, fileName)
    }
    
    private fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "Urdu_Paper_$timestamp.pdf"
    }
}
