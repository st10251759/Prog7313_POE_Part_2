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
 Title: Save data in a local database using Room  |  App data and files  |  Android Developers
 Author: Android Developer
 Date Published: 2019
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://developer.android.com/training/data-storage/room
  --------------------------------Code Attribution----------------------------------

  --------------------------------Code Attribution----------------------------------
 Title: Basic syntax | Kotlin Documentation
 Author: Kotlin
 Date Published: 06 November 2024
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://kotlinlang.org/docs/basic-syntax.html
  --------------------------------Code Attribution----------------------------------
*/

//Imports
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.adapters.ExpenseListAdapter
import com.firstproject.prog7313_budgetbuddy.data.entities.Expense
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 --------------------------------Code Attribution----------------------------------
 Title: Adapter  |  API reference  |  Android Developers
 Author: Android Developer
 Date Published: 2019
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://developer.android.com/reference/android/widget/Adapter
  --------------------------------Code Attribution----------------------------------
*/

// This activity displays a list of expenses for the current user within a selected date range
class ExpenseListActivity : AppCompatActivity(), ExpenseListAdapter.ExpenseClickListener {
    //Variables for Firebase, ViewModels and ExpenseAdapter Class
    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ExpenseListAdapter

    // UI Components
    private lateinit var rvExpenses: RecyclerView
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var tvTotalExpenses: TextView
    private lateinit var btnBack: ImageButton

    // Date related variables
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private var fromDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1) // First day of current month
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    // Initialize toDate to the last day of the current month at 23:59:59
    private var toDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) // Last day of current month
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_list)
        // Apply window insets for edge-to-edge display
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

        // Load expenses for the default date range (current month)
        loadExpenses()
    }

    // Initialize and bind UI components
    private fun initializeUI() {
        rvExpenses = findViewById(R.id.rvExpenses)
        etFromDate = findViewById(R.id.etFromDate)
        etToDate = findViewById(R.id.etToDate)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        btnBack = findViewById(R.id.btnBack)

        // Setup RecyclerView with adapter
        adapter = ExpenseListAdapter(emptyList(), this)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter

        // Display default date range
        updateDateDisplays()
    }

    // Attach listeners to buttons and fields
    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // From Date picker
        etFromDate.setOnClickListener {
            showDatePicker(true)
        }

        // To Date picker
        etToDate.setOnClickListener {
            showDatePicker(false)
        }
    }

    // Display a date picker dialog
    private fun showDatePicker(isFromDate: Boolean) {
        val calendar = if (isFromDate) fromDate else toDate

        // Update calendar with selected date
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                if (isFromDate) {
                    // Set time to start of day
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    fromDate = calendar
                } else {
                    // Set time to end of day
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    toDate = calendar
                }

                // Refresh UI and data
                updateDateDisplays()
                loadExpenses()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Validation to Limit date range selection
        // If setting "to date", limit the minimum date to the "from date"
        if (!isFromDate) {
            datePickerDialog.datePicker.minDate = fromDate.timeInMillis
        }

        // If setting "from date", limit the maximum date to the "to date" or today
        if (isFromDate) {
            datePickerDialog.datePicker.maxDate = toDate.timeInMillis
        }

        datePickerDialog.show()
    }

    // Update the displayed "From" and "To" dates
    private fun updateDateDisplays() {
        etFromDate.setText(dateFormatter.format(fromDate.time))
        etToDate.setText(dateFormatter.format(toDate.time))
    }

    // Fetch expenses from database for the selected date range
    private fun loadExpenses() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            viewModel.getExpensesByPeriod(userId, fromDate.time, toDate.time).observe(this) { expenses ->
                adapter.updateExpenses(expenses)

                // Update total amount
                updateTotalAmount(expenses)
            }
        }
    }

    // Calculate and display the total amount of expenses
    private fun updateTotalAmount(expenses: List<Expense>) {
        val total = expenses.sumOf { it.totalAmount }
        val formattedTotal = String.format(Locale.getDefault(), "R%,.2f", total)
        tvTotalExpenses.text = formattedTotal
    }

    override fun onExpenseClicked(expense: Expense) {
        // Handle expense item click if needed
        // For example, show expense details or allow editing - Will be used in Part 3
    }

    // Callback when the "Download Receipt" button is clicked
    override fun onDownloadReceiptClicked(expense: Expense) {
        expense.photoId?.let { photoIdStr ->
            lifecycleScope.launch {
                try {
                    // Convert photoId to Int safely
                    val photoId = photoIdStr.toIntOrNull()

                    if (photoId == null) {
                        Toast.makeText(
                            this@ExpenseListActivity,
                            "Invalid receipt ID",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    // Log the photoId for debugging
                    Log.d("ExpenseListActivity", "Attempting to retrieve photo with ID: $photoId")

                    // Safely get the photo using the suspend function
                    val photo = viewModel.getPhotoById(photoId)

                    if (photo == null) {
                        Log.e("ExpenseListActivity", "No photo found for ID: $photoId")
                        Toast.makeText(
                            this@ExpenseListActivity,
                            "No receipt found for this expense",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    // Log the file path for debugging
                    Log.d("ExpenseListActivity", "Photo file path: ${photo.filePath}")

                    val photoFile = File(photo.filePath)

                    // Enhanced file existence check
                    if (!photoFile.exists()) {
                        Log.e("ExpenseListActivity", "Photo file does not exist: ${photoFile.absolutePath}")
                        Toast.makeText(
                            this@ExpenseListActivity,
                            "Receipt image file not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    // Check if file exists and is valid
                    if (!photoFile.isFile) {
                        Log.e("ExpenseListActivity", "Path is not a file: ${photoFile.absolutePath}")
                        Toast.makeText(
                            this@ExpenseListActivity,
                            "Invalid receipt image",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    // Create content URI using FileProvider
                    val photoUri = FileProvider.getUriForFile(
                        this@ExpenseListActivity,
                        "${applicationContext.packageName}.fileprovider",
                        photoFile
                    )

                    // Create intent to view the image
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(photoUri, "image/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    // Try multiple ways to view the image
                    try {
                        // First, try to start the activity directly
                        startActivity(intent)
                    } catch (activityNotFoundException: ActivityNotFoundException) {
                        // If no activity found, try with chooser
                        val chooserIntent = Intent.createChooser(
                            intent,
                            "Open Receipt with..."
                        )

                        try {
                            startActivity(chooserIntent)
                        } catch (e: Exception) {
                            // If all else fails, show a message
                            Log.e("ExpenseListActivity", "Failed to open image", e)
                            Toast.makeText(
                                this@ExpenseListActivity,
                                "Unable to open receipt. No compatible apps found.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    // Handle any unexpected errors
                    Log.e("ExpenseListActivity", "Error accessing receipt", e)
                    Toast.makeText(
                        this@ExpenseListActivity,
                        "Error accessing receipt: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } ?: run {
            // No photo ID attached to expense
            Toast.makeText(
                this,
                "No receipt available for this expense",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}