package com.firstproject.prog7313_budgetbuddy

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.firstproject.prog7313_budgetbuddy.data.entities.Category
import com.firstproject.prog7313_budgetbuddy.data.entities.Expense
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var tvTotalAmount: TextView
    private lateinit var etDate: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var etDescription: EditText
    private lateinit var ivReceiptPhoto: ImageView
    private lateinit var btnAddPhoto: Button
    private lateinit var btnSaveExpense: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnToday: Button
    private lateinit var btnAddCategory: ImageButton

    // Keypad buttons
    private lateinit var numButtons: List<Button>
    private lateinit var btnDot: Button
    private lateinit var btnDelete: Button

    // Data
    private var currentAmount = "0.00"
    private var selectedDate = Calendar.getInstance()
    private var selectedCategoryId: Int? = null
    private var selectedCategoryName: String = ""
    private var categories = listOf<Category>()
    private var photoUri: Uri? = null
    private var photoFilePath: String? = null

    // Date formatters
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    // Activity result launchers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            displayPhoto()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                photoUri = uri
                // Copy to our app's storage and get file path
                photoFilePath = copyUriToFile(uri)
                displayPhoto()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expense)

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize UI components
        initializeUI()
        setupListeners()

        // Load categories
        loadCategories()
    }

    private fun initializeUI() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        etDate = findViewById(R.id.etDate)
        categorySpinner = findViewById(R.id.categorySpinner)
        etDescription = findViewById(R.id.etDescription)
        ivReceiptPhoto = findViewById(R.id.ivReceiptPhoto)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        btnBack = findViewById(R.id.btnBack)
        btnToday = findViewById(R.id.btnToday)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        // Initialize numeric keypad
        numButtons = listOf(
            findViewById(R.id.btn0),
            findViewById(R.id.btn1),
            findViewById(R.id.btn2),
            findViewById(R.id.btn3),
            findViewById(R.id.btn4),
            findViewById(R.id.btn5),
            findViewById(R.id.btn6),
            findViewById(R.id.btn7),
            findViewById(R.id.btn8),
            findViewById(R.id.btn9)
        )
        btnDot = findViewById(R.id.btnDot)
        btnDelete = findViewById(R.id.btnDelete)

        //Set the background color to grey in backend to override the layout
        btnToday.setBackgroundColor(Color.parseColor("#D3D3D3"))


        // Set default date to today
        updateAmountDisplay()
        updateDateDisplay()
    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Today button
        btnToday.setOnClickListener {
            selectedDate = Calendar.getInstance()
            updateDateDisplay()
        }

        // Date picker
        etDate.setOnClickListener {
            showDatePicker()
        }

        // Add Category button
        btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        // Add Photo button
        btnAddPhoto.setOnClickListener {
            showPhotoOptionsDialog()
        }

        // Save Expense button
        btnSaveExpense.setOnClickListener {
            saveExpense()
        }

        // Set up numeric keypad
        setupNumericKeypad()
    }

    private fun setupNumericKeypad() {
        for (i in 0..9) {
            numButtons[i].setOnClickListener { addDigit(i.toString()) }
        }

        btnDot.setOnClickListener { addDecimalPoint() }
        btnDelete.setOnClickListener { deleteLastDigit() }
    }

    private fun addDigit(digit: String) {
        if (currentAmount == "0.00") currentAmount = ""
        val newAmount = currentAmount + digit
        if (isValidCurrencyFormat(newAmount)) {
            currentAmount = newAmount
            updateAmountDisplay()
        }
    }

    private fun addDecimalPoint() {
        if (!currentAmount.contains(".")) {
            currentAmount += "."
            updateAmountDisplay()
        }
    }

    private fun deleteLastDigit() {
        if (currentAmount.isNotEmpty()) {
            currentAmount = currentAmount.dropLast(1)
            if (currentAmount.isEmpty()) currentAmount = "0.00"
            updateAmountDisplay()
        }
    }

    private fun isValidCurrencyFormat(input: String): Boolean {
        return input.matches(Regex("\\d{0,7}(\\.\\d{0,2})?"))
    }

    private fun updateAmountDisplay() {
        tvTotalAmount.text = currentAmount
    }

    private fun loadCategories() {
        viewModel.getAllCategories().observe(this) { categoryList ->
            categories = categoryList

            if (categoryList.isEmpty()) {
                // If no categories exist, show a message and prompt to create one
                val noCategoriesAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listOf("No categories available")
                )
                noCategoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = noCategoriesAdapter

                Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show()
                return@observe
            }

            val categoryNames = categoryList.map { it.categoryName }

            // Create a custom spinner adapter with better styling
            val adapter = object : ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    (view as TextView).apply {
                        setPadding(16, 16, 16, 16)
                        text = if (position >= 0 && position < categoryNames.size) {
                            categoryNames[position]
                        } else {
                            "Select category"
                        }
                    }
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    (view as TextView).apply {
                        setPadding(16, 16, 16, 16)
                    }
                    return view
                }
            }

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            // Select listener
            categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position >= 0 && position < categories.size) {
                        selectedCategoryId = categories[position].categoryId
                        selectedCategoryName = categories[position].categoryName
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedCategoryId = null
                    selectedCategoryName = ""
                }
            }
        }
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateDateDisplay()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showAddCategoryDialog() {
        // 1) inflate
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etCategoryName    = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val rg1               = dialogView.findViewById<RadioGroup>(R.id.colorOptions)
        val rg2               = dialogView.findViewById<RadioGroup>(R.id.colorOptionsRow2)
        val btnSave           = dialogView.findViewById<Button>(R.id.btnSaveCategory)
        val btnCancel         = dialogView.findViewById<Button>(R.id.btnCancelCategory)
        val btnBackCategory   = dialogView.findViewById<ImageView>(R.id.btnBackCategory)

        // 2) make them mutually exclusive
        rg1.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) rg2.clearCheck()
        }
        rg2.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) rg1.clearCheck()
        }

        // 3) build dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.setCancelable(true)

        // 4) save
        btnSave.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // pick whichever is checked in rg1 or rg2 (they’re now mutually exclusive)
            val chosenId = rg1.checkedRadioButtonId.takeIf { it != -1 }
                ?: rg2.checkedRadioButtonId
            val colorHex = if (chosenId != -1) {
                (dialogView.findViewById<RadioButton>(chosenId).tag as? String)
                    ?: "#74D3AE"  // fallback, shouldn’t happen
            } else {
                "#74D3AE"      // you could also force the user to pick at least one
            }

            saveCategory(name, colorHex)
            dialog.dismiss()
        }

        // 5) cancel & back both just close
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnBackCategory.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }



    private fun saveCategory(name: String, colorHex: String) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createCategory(name, colorHex)
        Toast.makeText(this, "Category created", Toast.LENGTH_SHORT).show()
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Receipt Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        photoFile?.let {
            photoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                it
            )
            photoFilePath = it.absolutePath
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "RECEIPT_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun copyUriToFile(uri: Uri): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val destFile = File(storageDir, "RECEIPT_${timeStamp}.jpg")

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun displayPhoto() {
        photoUri?.let {
            ivReceiptPhoto.setImageURI(it)
            ivReceiptPhoto.visibility = View.VISIBLE
            btnAddPhoto.visibility = View.GONE
        }
    }

    private fun updateDateDisplay() {
        etDate.setText(dateFormatter.format(selectedDate.time))
    }

    private fun formatAndDisplayAmount() {
        // Parse to double to format correctly
        try {
            // Special handling for amounts ending with decimal point
            val amount = if (currentAmount.endsWith(".")) {
                // Preserve the decimal point for display
                tvTotalAmount.text = currentAmount
                return
            } else {
                currentAmount.toDouble()
            }

            // Format with two decimal places
            val formattedAmount = String.format(Locale.getDefault(), "%.2f", amount)
            tvTotalAmount.text = formattedAmount

            // Update the current amount but only if it doesn't end with a decimal point
            if (!currentAmount.endsWith(".")) {
                currentAmount = formattedAmount
            }
        } catch (e: NumberFormatException) {
            // Handle formatting error
            tvTotalAmount.text = "0.00"
            currentAmount = "0.00"
        }
    }

    private fun saveExpense() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate inputs
        if (categories.isEmpty()) {
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show()
            showAddCategoryDialog()
            return
        }

        if (selectedCategoryId == null && selectedCategoryName.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val description = etDescription.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            currentAmount.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Create expense object
        val expense = Expense(
            userId = userId,
            categoryId = selectedCategoryId,
            category = selectedCategoryName,
            expenseDate = selectedDate.time,
            startTime = null,  // Not needed in this UI
            endTime = null,    // Not needed in this UI
            description = description,
            totalAmount = amount,
            photoId = null // We'll update this if photo is saved
        )

        // Save the expense with photo if available
        viewModel.createExpense(
            categoryId = selectedCategoryId,
            categoryName = selectedCategoryName,
            expenseDate = selectedDate.time,
            startTime = null,
            endTime = null,
            description = description,
            amount = amount,
            photoPath = photoFilePath
        )

        Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}