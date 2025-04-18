package com.firstproject.prog7313_budgetbuddy

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

class CategorySpendingActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: CategorySpendingAdapter

    // UI Components
    private lateinit var rvCategories: RecyclerView
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var tvTotalSpending: TextView
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

        // Set up support action bar
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

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize UI components
        initializeUI()
        setupListeners()

        // Load expenses for the default date range (current month)
        loadCategorySpending()
    }

    private fun initializeUI() {
        rvCategories = findViewById(R.id.rvCategories)
        etFromDate = findViewById(R.id.etFromDate)
        etToDate = findViewById(R.id.etToDate)
        tvTotalSpending = findViewById(R.id.tvTotalSpending)
        btnBack = findViewById(R.id.btnBack)

        // Setup RecyclerView
        adapter = CategorySpendingAdapter(emptyList())
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        // Initialize date fields with current month range
        updateDateDisplays()
    }

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

    private fun showDatePicker(isFromDate: Boolean) {
        val calendar = if (isFromDate) fromDate else toDate
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
                updateDateDisplays()
                loadCategorySpending()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

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

    private fun updateDateDisplays() {
        etFromDate.setText(dateFormatter.format(fromDate.time))
        etToDate.setText(dateFormatter.format(toDate.time))
    }

    private fun loadCategorySpending() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Get categories with their spending amounts for the selected period
            viewModel.getCategorySpendingForPeriod(userId, fromDate.time, toDate.time).observe(this) { categorySpending ->
                adapter.updateCategories(categorySpending)

                // Update total amount
                updateTotalSpending(categorySpending)
            }
        }
    }

    private fun updateTotalSpending(categories: List<CategoryWithAmount>) {
        val total = categories.sumOf { it.amount }
        val formattedTotal = String.format(Locale.getDefault(), "R%,.2f", total)
        tvTotalSpending.text = formattedTotal
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}