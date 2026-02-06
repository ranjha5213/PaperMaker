package com.example.urdupapermaker.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urdupapermaker.R
import com.example.urdupapermaker.adapters.QuestionAdapter
import com.example.urdupapermaker.databinding.ActivityMainBinding
import com.example.urdupapermaker.models.Question
import com.example.urdupapermaker.utils.FileParser
import com.example.urdupapermaker.utils.PermissionHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var questionAdapter: QuestionAdapter
    private var questionList = mutableListOf<Question>()
    private var selectedQuestions = mutableListOf<Question>()
    
    private val permissionHelper = PermissionHelper(this)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            showFilePicker()
        } else {
            Snackbar.make(
                binding.root,
                "Storage permission is required to read files",
                Snackbar.LENGTH_LONG
            ).setAction("SETTINGS") {
                openAppSettings()
            }.show()
        }
    }
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            importQuestionsFromUri(it)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupSpinners()
        setupClickListeners()
        loadSampleQuestions()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "اردو پرچہ میکر"
    }
    
    private fun setupRecyclerView() {
        questionAdapter = QuestionAdapter(questionList) { question, position ->
            question.isSelected = !question.isSelected
            if (question.isSelected) {
                selectedQuestions.add(question)
            } else {
                selectedQuestions.remove(question)
            }
            updateSelectionCount()
            questionAdapter.notifyItemChanged(position)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = questionAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupSpinners() {
        // Selection mode spinner
        val modes = arrayOf("دستی انتخاب", "خودکار انتخاب")
        val modeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modes)
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMode.adapter = modeAdapter
        
        // Question count spinner
        val counts = (5..50 step 5).map { it.toString() }
        val countAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, counts)
        countAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCount.adapter = countAdapter
        binding.spinnerCount.setSelection(3) // Default 20 questions
        
        binding.spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.spinnerCount.isEnabled = position == 1 // Enable only for auto mode
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddQuestion.setOnClickListener {
            startActivity(Intent(this, QuestionEditorActivity::class.java))
        }
        
        binding.btnImportFile.setOnClickListener {
            checkAndRequestPermissions()
        }
        
        binding.btnGeneratePaper.setOnClickListener {
            generatePaper()
        }
        
        binding.btnSelectAll.setOnClickListener {
            selectAllQuestions()
        }
        
        binding.btnClearSelection.setOnClickListener {
            clearSelection()
        }
        
        binding.btnRandomSelect.setOnClickListener {
            selectRandomQuestions()
        }
    }
    
    private fun checkAndRequestPermissions() {
        if (permissionHelper.hasStoragePermission()) {
            showFilePicker()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }
    
    private fun showFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "text/csv"))
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun importQuestionsFromUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val content = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                content?.let { text ->
                    val parser = FileParser()
                    val questions = parser.parseText(text)
                    
                    withContext(Dispatchers.Main) {
                        if (questions.isNotEmpty()) {
                            questionList.addAll(questions)
                            questionAdapter.notifyDataSetChanged()
                            updateQuestionCount()
                            
                            Snackbar.make(
                                binding.root,
                                "${questions.size} سوالات درآمد ہو گئے",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "فائل میں کوئی درست سوالات نہیں ملیں",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        binding.root,
                        "فائل پڑھنے میں خرابی: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun loadSampleQuestions() {
        // Load some sample questions for demo
        val sampleQuestions = listOf(
            Question(
                questionText = "کیا اسلام کا بنیادی عقیدہ کیا ہے؟",
                optionA = "توحید",
                optionB = "رسالت",
                optionC = "آخرت",
                optionD = "نماز"
            ),
            Question(
                questionText = "قرآن مجید کس پر نازل ہوا؟",
                optionA = "حضرت محمد ﷺ",
                optionB = "حضرت موسیٰ",
                optionC = "حضرت عیسیٰ",
                optionD = "حضرت ابراہیم"
            ),
            Question(
                questionText = "فرض نمازوں کی کل تعداد کتنی ہے؟",
                optionA = "5",
                optionB = "3",
                optionC = "4",
                optionD = "6"
            )
        )
        
        questionList.addAll(sampleQuestions)
        questionAdapter.notifyDataSetChanged()
        updateQuestionCount()
    }
    
    private fun selectAllQuestions() {
        questionList.forEach { it.isSelected = true }
        selectedQuestions.clear()
        selectedQuestions.addAll(questionList)
        updateSelectionCount()
        questionAdapter.notifyDataSetChanged()
    }
    
    private fun clearSelection() {
        questionList.forEach { it.isSelected = false }
        selectedQuestions.clear()
        updateSelectionCount()
        questionAdapter.notifyDataSetChanged()
    }
    
    private fun selectRandomQuestions() {
        val count = binding.spinnerCount.selectedItem.toString().toIntOrNull() ?: 20
        if (questionList.size < count) {
            Snackbar.make(
                binding.root,
                "صرف ${questionList.size} سوالات موجود ہیں",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        
        clearSelection()
        val randomQuestions = questionList.shuffled().take(count)
        randomQuestions.forEach { it.isSelected = true }
        selectedQuestions.addAll(randomQuestions)
        updateSelectionCount()
        questionAdapter.notifyDataSetChanged()
    }
    
    private fun generatePaper() {
        val questionsToUse = if (binding.spinnerMode.selectedItemPosition == 0) {
            // Manual selection
            if (selectedQuestions.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    "کم از کم ایک سوال منتخب کریں",
                    Snackbar.LENGTH_SHORT
                ).show()
                return
            }
            selectedQuestions
        } else {
            // Auto selection
            val count = binding.spinnerCount.selectedItem.toString().toIntOrNull() ?: 20
            if (questionList.size < count) {
                Snackbar.make(
                    binding.root,
                    "صرف ${questionList.size} سوالات موجود ہیں",
                    Snackbar.LENGTH_SHORT
                ).show()
                return
            }
            questionList.shuffled().take(count)
        }
        
        val schoolName = binding.etSchoolName.text.toString().trim()
        val className = binding.etClassName.text.toString().trim()
        val subject = binding.etSubject.text.toString().trim()
        
        if (schoolName.isEmpty() || className.isEmpty()) {
            Snackbar.make(
                binding.root,
                "براہ کرم اسکول اور کلاس کا نام درج کریں",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        
        val intent = Intent(this, PaperPreviewActivity::class.java).apply {
            putExtra("questions", ArrayList(questionsToUse))
            putExtra("schoolName", schoolName)
            putExtra("className", className)
            putExtra("subject", subject)
        }
        startActivity(intent)
    }
    
    private fun updateQuestionCount() {
        binding.tvQuestionCount.text = "کل سوالات: ${questionList.size}"
    }
    
    private fun updateSelectionCount() {
        binding.tvSelectedCount.text = "منتخب شدہ: ${selectedQuestions.size}"
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_about -> {
                showAboutDialog()
                true
            }
            R.id.menu_export_all -> {
                exportAllQuestions()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
       private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("اردو پرچہ میکر")
            .setMessage("Version: 1.0.0\n\n" +
                "• متن فائل سے سوالات درآمد کریں\n" +
                "• CSV فائل سے سوالات درآمد کریں\n" +
                "• دستی/خودکار سوالات کا انتخاب\n" +
                "• PDF پرچہ جنریٹ کریں\n" +
                "• پرنٹ کریں\n\n" +
                "Developer: Urdu Paper Maker Team\n" +
                "Contact: support@urdupapermaker.com")
            .setPositiveButton("ٹھیک ہے", null)
            .show()
    }

    
    private fun exportAllQuestions() {
        // Export all questions to CSV
        // Implementation would go here
    }
}
