package com.firstproject.prog7313_budgetbuddy

/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 2
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: Basic syntax | Kotlin Documentation
 Author: Kotlin
 Date Published: 06 November 2024
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://kotlinlang.org/docs/basic-syntax.html
  --------------------------------Code Attribution----------------------------------
*/

// Import necessary Android and Kotlin libraries
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
import android.util.Log  //import for logging
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

    // Declare variables ViewModel and Firebase authentication
    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // Variables forUI Components
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

    // Variables for Numeric keypad buttons
    private lateinit var numButtons: List<Button>
    private lateinit var btnDot: Button
    private lateinit var btnDelete: Button

    // Variables for Expense data
    private var currentAmount = "0.00"
    private var selectedDate = Calendar.getInstance()
    private var selectedCategoryId: Int? = null
    private var selectedCategoryName: String = ""
    private var categories = listOf<Category>()
    private var photoUri: Uri? = null
    private var photoFilePath: String? = null

    // Formatter for displaying dates
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    // Launcher for taking a new picture
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            displayPhoto()
        }
    }

    /*
       --------------------------------Code Attribution----------------------------------
        Title: Data and file storage overview  |  App data and files  |  Android Developers
        Author: Android Developers
        Date Published: 2019
        Date Accessed: 17 April 2025
        Code Version: v21.20
        Availability: https://developer.android.com/training/data-storage
       --------------------------------Code Attribution----------------------------------
    */
    // Launcher for picking an image from the gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                photoUri = uri
                // Copy to our app's storage and get file path - Save image locally and display it
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

        /*
       --------------------------------Code Attribution----------------------------------
        Title: Get Started with Firebase Authentication on Android  |  Firebase
        Author: Firebase
        Date Published: 2019
        Date Accessed: 15 April 2025
        Code Version: N/A
        Availability: https://firebase.google.com/docs/auth/android/start
       --------------------------------Code Attribution----------------------------------
        */
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

        // Call the method to Set up numeric keypad
        setupNumericKeypad()
    }

    // Configure the number pad buttons for entering amounts
    private fun setupNumericKeypad() {
        // Loop through the digits 0 to 9
        for (i in 0..9) {
            // Assign a click listener to each number button
            // When clicked, the corresponding digit is added to the input
            numButtons[i].setOnClickListener { addDigit(i.toString()) }
        }

        // Assign a click listener to the dot (.) button
        // When clicked, a decimal point is added to the input
        btnDot.setOnClickListener { addDecimalPoint() }

        // Assign a click listener to the delete (←) button
        // When clicked, the last digit or character is removed from the input
        btnDelete.setOnClickListener { deleteLastDigit() }
    }

    // Adds a digit to the current amount entered by the user
    private fun addDigit(digit: String) {
        // If the current amount is the default "0.00", reset it to an empty string
        // so that the new digit replaces it rather than appending to it
        if (currentAmount == "0.00") currentAmount = ""
        // Create a new string by appending the entered digit to the current amount
        val newAmount = currentAmount + digit
        // Check if the new amount is in a valid currency format (e.g., not too many decimals)
        if (isValidCurrencyFormat(newAmount)) {
            // If valid, update the currentAmount to the new value
            currentAmount = newAmount
            // Call Method to Refresh the UI to reflect the new amount
            updateAmountDisplay()
        }
    }

    // Adds a decimal point to the amount, if one isn't already present
    private fun addDecimalPoint() {
        // Only add a decimal point if it doesn't already exist in the current amount
        if (!currentAmount.contains(".")) {
            // Append the decimal point to the current amount string
            currentAmount += "."
            // Update the UI to reflect the change
            updateAmountDisplay()
        }
    }

    // Deletes the last character (digit or decimal point) from the current amount
    private fun deleteLastDigit() {
        // Check if there's anything to delete
        if (currentAmount.isNotEmpty()) {
            // Remove the last character from the string
            currentAmount = currentAmount.dropLast(1)
            // If the string becomes empty after deletion, reset it to the default "0.00"
            if (currentAmount.isEmpty()) currentAmount = "0.00"
            // Update the UI to reflect the new value
            updateAmountDisplay()
        }
    }

    // Validates whether the entered currency string follows the correct format
    private fun isValidCurrencyFormat(input: String): Boolean {
        // This regex allows:
        // - Up to 7 digits before a decimal point (\\d{0,7})
        // - An optional decimal point followed by up to 2 digits (\\.\\d{0,2})?
        // Example valid inputs: "123", "123.45", "0.5", "", "0"
        return input.matches(Regex("\\d{0,7}(\\.\\d{0,2})?"))
    }

    // Updates the UI component (TextView) that displays the total amount
    private fun updateAmountDisplay() {
        // Sets the text of the tvTotalAmount TextView to the currentAmount string
        tvTotalAmount.text = currentAmount
    }

    // Loads all available expense categories from the ViewModel and sets up the spinner dropdown
    private fun loadCategories() {
        // Observe LiveData from the ViewModel to get the list of all categories
        viewModel.getAllCategories().observe(this) { categoryList ->
            categories = categoryList

            // If no categories are returned, show a placeholder spinner and a Toast message
            if (categoryList.isEmpty()) {
                // Show default text in spinner
                val noCategoriesAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listOf("No categories available")
                )
                noCategoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply adapter to the spinner
                categorySpinner.adapter = noCategoriesAdapter

                // Validation to Notify the user to create a category before adding an expense
                Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show()
                // Exit the observer early since there's nothing to display
                return@observe
            }
            // Extract just the category names for the spinner
            val categoryNames = categoryList.map { it.categoryName }

            // Create a custom spinner adapter with padded items for a cleaner UI
            /*
                --------------------------------Code Attribution----------------------------------
                Title: Add spinners to your app  |  Views  |  Android Developers
                Author: Android Developers
                Date Published: 2019
                Date Accessed: 17 April 2025
                Code Version: v21.20
                Availability: https://developer.android.com/develop/ui/views/components/spinner
                --------------------------------Code Attribution----------------------------------
            */

            val adapter = object : ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
            ) {
                // Customize the main view shown in the spinner
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    (view as TextView).apply {
                        setPadding(16, 16, 16, 16)
                        // Set the visible text in the spinner for each item
                        text = if (position >= 0 && position < categoryNames.size) {
                            categoryNames[position]
                        } else {
                            "Select category"
                        }
                    }
                    return view
                }

                // Customize the view shown in the dropdown list
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    (view as TextView).apply {
                        setPadding(16, 16, 16, 16)
                    }
                    return view
                }
            }

            // Set dropdown layout style and apply the adapter to the spinner
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            // Handle selection events when the user picks a category from the dropdown
            categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // If the selection is valid, store the category ID and name
                    if (position >= 0 && position < categories.size) {
                        selectedCategoryId = categories[position].categoryId
                        selectedCategoryName = categories[position].categoryName
                    }
                }

                // If no selection is made, clear the selected values
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedCategoryId = null
                    selectedCategoryName = ""
                }
            }
        }
    }

    // Opens a date picker dialog to allow the user to choose a date for the expense
    private fun showDatePicker() {
        // Get the currently selected date values (defaults to today's date)
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        // Create a DatePickerDialog with the current date preselected
        val datePickerDialog = DatePickerDialog(
            // Callback listener: update the selectedDate when the user picks a new date
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateDateDisplay()
            },
            // Pass initial values to the dialog
            year,
            month,
            day
        )
        // Display the dialog to the user
        datePickerDialog.show()
    }

    // Function to show a dialog allowing users to add a new category
    private fun showAddCategoryDialog() {
        // 1) Inflate the layout for the dialog from XML
        // Initialize references to UI components within the dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etCategoryName    = dialogView.findViewById<EditText>(R.id.etCategoryName)           // Input for category name
        val rg1               = dialogView.findViewById<RadioGroup>(R.id.colorOptions)           // First row of color choices
        val rg2               = dialogView.findViewById<RadioGroup>(R.id.colorOptionsRow2)       // Second row of color choices
        val btnSave           = dialogView.findViewById<Button>(R.id.btnSaveCategory)            // Button to save the new category
        val btnCancel         = dialogView.findViewById<Button>(R.id.btnCancelCategory)          // Button to cancel and close the dialog
        val btnBackCategory   = dialogView.findViewById<ImageView>(R.id.btnBackCategory)          // Image button to go back (closes dialog)

        // 2) Make sure the two radio groups are mutually exclusive
        // If a button in rg1 is checked, clear rg2
        rg1.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) rg2.clearCheck()
        }
        // If a button in rg2 is checked, clear rg1
        rg2.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) rg1.clearCheck()
        }

        // 3) Build and configure the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.setCancelable(true)

        // 4) Logic for when the Save button is clicked
        btnSave.setOnClickListener {
            // Get and trim category name
            val name = etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                // Show error if category name is blank
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determine which color was selected (from either group)
            val chosenId = rg1.checkedRadioButtonId.takeIf { it != -1 }
                ?: rg2.checkedRadioButtonId
            // Extract the color value using the tag attribute from the selected radio button
            val colorHex = if (chosenId != -1) {
                (dialogView.findViewById<RadioButton>(chosenId).tag as? String)
                    ?: "#74D3AE"  // Default fallback color
            } else {
                "#74D3AE"      // Default if no color selected
            }

            // Call function to save category and close dialog
            saveCategory(name, colorHex)
            dialog.dismiss()
        }

        // 5) Cancel and back buttons simply close the dialog
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnBackCategory.setOnClickListener { dialog.dismiss() }

        // Display the dialog to the user
        dialog.show()
    }

    // Saves a new category to the backend using ViewModel
    private fun saveCategory(name: String, colorHex: String) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        // Call ViewModel method to create category
        viewModel.createCategory(name, colorHex)
        Toast.makeText(this, "Category created", Toast.LENGTH_SHORT).show()
    }

    // Shows options to take a photo or select from gallery
    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Receipt Photo")
            .setItems(options) { _, which ->
                when (which) {
                    // User chose to take a photo
                    0 -> takePhoto()
                    // User chose from gallery
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }

    /*
       --------------------------------Code Attribution----------------------------------
        Title: Data and file storage overview  |  App data and files  |  Android Developers
        Author: Android Developers
        Date Published: 2019
        Date Accessed: 17 April 2025
        Code Version: v21.20
        Availability: https://developer.android.com/training/data-storage
       --------------------------------Code Attribution----------------------------------
    */
    // Opens camera to take a photo and saves the image URI
    private fun takePhoto() {
        val photoFile = createImageFile() // Create temporary file for the image
        photoFile?.let {
            photoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                it
            )
            photoFilePath = it.absolutePath
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureLauncher.launch(takePictureIntent)  // Launch camera
        }
    }

    // Opens gallery to choose an image
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)  // Launch gallery
    }

    // Creates a temporary image file in the app's external picture directory
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "RECEIPT_${timeStamp}_",  // Prefix for filename
            ".jpg",  // File extension
            storageDir // Save location
        )
    }

    // Copies image from a URI into a temporary file in the app directory
    private fun copyUriToFile(uri: Uri): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val destFile = File(storageDir, "RECEIPT_${timeStamp}.jpg")

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)  // Copy content from URI to file
                }
            }
            return destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    // Displays the selected/taken photo in the UI
    private fun displayPhoto() {
        photoUri?.let {
            ivReceiptPhoto.setImageURI(it)   // Show image in ImageView
            ivReceiptPhoto.visibility = View.VISIBLE
            btnAddPhoto.visibility = View.GONE    // Show image in ImageView
        }
    }

    private fun updateDateDisplay() {
        etDate.setText(dateFormatter.format(selectedDate.time))
    }

    // Updates the date field using the selected date and formatter
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

    /*
     --------------------------------Code Attribution----------------------------------
     Title: Log  |  API reference  |  Android Developers
     Author: Android Developers
     Date Published: 2019
     Date Accessed: 24 April 2024
     Code Version: v21.20
     Availability: https://developer.android.com/reference/android/util/Log
      --------------------------------Code Attribution----------------------------------
    */

    // This function gathers all user inputs and saves a new expense entry to the database.
// It includes validation checks, user feedback via Toasts, and logs important steps using Android's Log utility.
    private fun saveExpense() {

        // Tag used for logging from this Activity. It helps filter log messages related to expense creation.
        val TAG = "AddExpenseActivity"

        // Check if the user is authenticated.
        // If not, show a message and exit early. Logging a warning because this is a user-level issue that shouldn't normally happen.
        val userId = auth.currentUser?.uid ?: run {
            Log.w(TAG, "User not authenticated") // Warn about the missing user authentication.
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if there are any existing categories to assign to this expense.
        // If no categories exist, show a prompt and trigger a dialog to add one.
        Log.d(TAG, "Checking if categories exist")
        if (categories.isEmpty()) {
            Log.w(TAG, "No categories available. Prompting to add a category.") // Log a warning for missing categories.
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show()
            showAddCategoryDialog()
            return
        }

        // Ensure the user has selected a category before saving the expense.
        // If not, show a message and exit early.
        if (selectedCategoryId == null && selectedCategoryName.isEmpty()) {
            Log.w(TAG, "No category selected.") // Warn that the user has not selected a category.
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate that a description has been entered by the user.
        // This is necessary context for the expense.
        val description = etDescription.text.toString().trim()
        if (description.isEmpty()) {
            Log.w(TAG, "Description is empty.") // Warn that the description input is missing.
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        // Attempt to parse the amount input into a valid double.
        // If parsing fails, log an error with the exception and exit early.
        val amount = try {
            currentAmount.toDouble()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid amount entered: $currentAmount", e) // Log error and include exception stack trace.
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Log key information before proceeding with saving.
        // This is useful for debugging and tracking the expense being created.
        Log.i(TAG, "Creating new expense with amount: $amount, description: $description, category: $selectedCategoryName")

        // Create the Expense data object with all necessary fields.
        val expense = Expense(
            userId = userId,
            categoryId = selectedCategoryId,
            category = selectedCategoryName,
            expenseDate = selectedDate.time,
            startTime = null,  // Not required in this version of the UI
            endTime = null,    // Not required in this version of the UI
            description = description,
            totalAmount = amount,
            photoId = null // Optional field, may be populated later if a receipt photo is attached
        )

        //  Instantiate the expense using the ViewModel.
        // All relevant fields are passed to ensure the record is saved completely.
        viewModel.createExpense(
            categoryId = selectedCategoryId,
            categoryName = selectedCategoryName,
            expenseDate = selectedDate.time,
            startTime = null,
            endTime = null,
            description = description,
            amount = amount,
            photoPath = photoFilePath  // Optional field for an image receipt
        )

        //  Log that the expense was successfully saved.
        // Logging the complete expense object helps with post-mortem analysis or debugging.
        Log.d(TAG, "Expense saved successfully!")

        // Step 10: Notify the user that the save was successful and close the screen.
        Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

}