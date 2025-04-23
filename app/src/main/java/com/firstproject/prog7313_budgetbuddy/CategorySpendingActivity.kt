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
*/

// Imports required Android and Room database libraries and classes.
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.adapters.CategorySpendingAdapter
import com.firstproject.prog7313_budgetbuddy.data.entities.CategoryWithAmount
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Activity that displays a user's spending grouped by categories within a selected date range.
class CategorySpendingActivity : AppCompatActivity() {
    // ViewModel that interacts with the repository to fetch category spending data.
    private lateinit var viewModel: ViewModels
    // FirebaseAuth instance for accessing the currently logged-in user
    private lateinit var auth: FirebaseAuth
    // Adapter to populate category spending data into the RecyclerView.
    private lateinit var adapter: CategorySpendingAdapter

    // UI Components
    private lateinit var rvCategories: RecyclerView
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var tvTotalSpending: TextView
    private lateinit var btnBack: ImageButton

    // Date formatter for displaying dates in MM/dd/yyyy format
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private var fromDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1) // First day of current month
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    // Default 'from' date set to the first day of the current month at 00:00:00
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
        setContentView(R.layout.activity_category_spending)

        // Set up support action bar and title
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Category Spending"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Get ViewModel instance
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Set up UI components and listeners
        initializeUI()
        setupListeners()

        // Load spending data for the default date range (current month)
        loadCategorySpending()
    }

    /*
     --------------------------------Code Attribution----------------------------------
    Title: Create dynamic lists with RecyclerView  |  Views  |  Android Developers
    Author: Android Developers
    Date Published: 2025
    Date Accessed: 18 April 2025
    Code Version: N/A
    Availability: https://developer.android.com/develop/ui/views/layout/recyclerview#next-steps
    --------------------------------Code Attribution----------------------------------
    */

    // Initializes all UI components and sets up RecyclerView
    private fun initializeUI() {
        rvCategories = findViewById(R.id.rvCategories)
        etFromDate = findViewById(R.id.etFromDate)
        etToDate = findViewById(R.id.etToDate)
        tvTotalSpending = findViewById(R.id.tvTotalSpending)
        btnBack = findViewById(R.id.btnBack)

        // Set up RecyclerView with an empty list and LinearLayoutManager
        adapter = CategorySpendingAdapter(emptyList())
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        // Initialize date fields with current month range
        updateDateDisplays()
    }

    // Sets click listeners for buttons and date pickers
    private fun setupListeners() {
        // Back button
        // Finish activity when back button is pressed
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

    // Shows a date picker dialog for selecting either the "from" or "to" date
    private fun showDatePicker(isFromDate: Boolean) {
        val calendar = if (isFromDate) fromDate else toDate
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                // Adjust time depending on whether it's from or to date
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
                // Update the date display and reload data
                updateDateDisplays()
                loadCategorySpending()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Validation Prevent users from selecting invalid date ranges
        if (!isFromDate) {
            datePickerDialog.datePicker.minDate = fromDate.timeInMillis
        }

        // If setting "from date", limit the maximum date to the "to date" or today
        if (isFromDate) {
            datePickerDialog.datePicker.maxDate = toDate.timeInMillis
        }

        datePickerDialog.show()
    }

    // Updates EditText fields with selected dates
    private fun updateDateDisplays() {
        etFromDate.setText(dateFormatter.format(fromDate.time))
        etToDate.setText(dateFormatter.format(toDate.time))
    }

    // Loads spending data for selected date range for the current user
    private fun loadCategorySpending() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Get categories with their spending amounts for the selected period
            viewModel.getCategorySpendingForPeriod(userId, fromDate.time, toDate.time).observe(this) { categorySpending ->
                // Update RecyclerView with new data
                adapter.updateCategories(categorySpending)

                // Calculate and display total spending
                updateTotalSpending(categorySpending)
            }
        }
    }

    // Calculates total spending across all categories and updates UI
    private fun updateTotalSpending(categories: List<CategoryWithAmount>) {
        val total = categories.sumOf { it.amount }
        val formattedTotal = String.format(Locale.getDefault(), "R%,.2f", total)
        tvTotalSpending.text = formattedTotal
    }

    // Handles back button click in the action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}