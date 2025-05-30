package com.firstproject.prog7313_budgetbuddy

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.adapters.ExpenseListAdapter
import com.firstproject.prog7313_budgetbuddy.data.models.*
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExpenseListActivity : BaseActivity(),
    ExpenseListAdapter.ExpenseClickListener,
    ExpenseFilterDialog.FilterApplyListener {

    //Variables for Firebase, ViewModels and ExpenseAdapter Class
    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ExpenseListAdapter
    private lateinit var btnThemeToggle: ImageButton

    // UI Components
    private lateinit var rvExpenses: RecyclerView
    private lateinit var etSearchBox: EditText
    private lateinit var btnFilter: ImageButton
    private lateinit var btnClearSearch: ImageButton
    private lateinit var tvFilterIndicator: TextView
    private lateinit var tvSearchResults: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var fabFilter: FloatingActionButton
    private lateinit var layoutSearchSuggestions: LinearLayout
    private lateinit var rvSearchSuggestions: RecyclerView

    // Search and Filter
    private var currentFilter = ExpenseFilter()
    private var isSearchActive = false
    private val searchSuggestions = mutableListOf<String>()

    private lateinit var chipToday: Chip
    private lateinit var chipThisWeek: Chip
    private lateinit var chipLast30Days: Chip
    private lateinit var chipHighAmounts: Chip

    // Date related variables
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_list)

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
        setupSearchFunctionality()

        // Load initial data
        loadExpenses()
        loadPopularSearchTerms()
    }

    private fun initializeUI() {
        rvExpenses = findViewById(R.id.rvExpenses)
        etSearchBox = findViewById(R.id.etSearchBox)
        btnFilter = findViewById(R.id.btnFilter)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        tvFilterIndicator = findViewById(R.id.tvFilterIndicator)
        tvSearchResults = findViewById(R.id.tvSearchResults)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        btnBack = findViewById(R.id.btnBack)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
        fabFilter = findViewById(R.id.fabFilter)
        layoutSearchSuggestions = findViewById(R.id.layoutSearchSuggestions)
        rvSearchSuggestions = findViewById(R.id.rvSearchSuggestions)

        // Initialize quick filter chips
        chipToday = findViewById(R.id.chipToday)
        chipThisWeek = findViewById(R.id.chipThisWeek)
        chipLast30Days = findViewById(R.id.chipLast30Days)
        chipHighAmounts = findViewById(R.id.chipHighAmounts)

        // Setup RecyclerView with adapter
        adapter = ExpenseListAdapter(emptyList(), this)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter

        setupThemeToggle(btnThemeToggle)
        updateFilterIndicator()
        setupQuickFilters()

    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Filter button
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        // Floating filter button
        fabFilter.setOnClickListener {
            showFilterDialog()
        }

        // Clear search
        btnClearSearch.setOnClickListener {
            clearSearch()
        }
    }

    private fun setupSearchFunctionality() {
        etSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()

                if (query.isNotEmpty()) {
                    isSearchActive = true
                    btnClearSearch.visibility = View.VISIBLE

                    // Update filter with search keyword
                    currentFilter = currentFilter.copy(searchKeyword = query)
                    loadExpenses()

                    // Get search suggestions
                    if (query.length >= 2) {
                        loadSearchSuggestions(query)
                    }

                    // Save search query
                    viewModel.saveSearchQuery(query)
                } else {
                    isSearchActive = false
                    btnClearSearch.visibility = View.GONE
                    layoutSearchSuggestions.visibility = View.GONE

                    // Clear search from filter
                    currentFilter = currentFilter.copy(searchKeyword = "")
                    loadExpenses()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etSearchBox.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && etSearchBox.text.isNotEmpty()) {
                loadSearchSuggestions(etSearchBox.text.toString())
            } else {
                layoutSearchSuggestions.visibility = View.GONE
            }
        }
    }

    private fun loadSearchSuggestions(query: String) {
        viewModel.getSearchSuggestions(query).observe(this) { suggestions ->
            if (suggestions.isNotEmpty() && etSearchBox.hasFocus()) {
                showSearchSuggestions(suggestions)
            } else {
                layoutSearchSuggestions.visibility = View.GONE
            }
        }
    }

    private fun showSearchSuggestions(suggestions: List<String>) {
        layoutSearchSuggestions.removeAllViews()

        suggestions.forEach { suggestion ->
            val suggestionView = TextView(this).apply {
                text = suggestion
                setPadding(32, 24, 32, 24)
                setTextColor(getColor(R.color.asparagus))
                textSize = 14f
                background = getDrawable(R.drawable.suggestion_item_background)

                setOnClickListener {
                    etSearchBox.setText(suggestion)
                    etSearchBox.clearFocus()
                    layoutSearchSuggestions.visibility = View.GONE
                }
            }

            layoutSearchSuggestions.addView(suggestionView)
        }

        layoutSearchSuggestions.visibility = View.VISIBLE
    }

    // Add this new method to setup quick filter functionality
    private fun setupQuickFilters() {
        // Today filter
        chipToday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherQuickFilters(chipToday)
                applyTodayFilter()
            } else if (!anyQuickFilterSelected()) {
                // If no quick filters are selected, reset to default
                resetToDefaultFilter()
            }
        }

        // This Week filter
        chipThisWeek.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherQuickFilters(chipThisWeek)
                applyThisWeekFilter()
            } else if (!anyQuickFilterSelected()) {
                resetToDefaultFilter()
            }
        }

        // Last 30 Days filter
        chipLast30Days.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherQuickFilters(chipLast30Days)
                applyLast30DaysFilter()
            } else if (!anyQuickFilterSelected()) {
                resetToDefaultFilter()
            }
        }

        // High Amounts filter (Over R100)
        chipHighAmounts.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherQuickFilters(chipHighAmounts)
                applyHighAmountsFilter()
            } else if (!anyQuickFilterSelected()) {
                resetToDefaultFilter()
            }
        }
    }

    private fun anyQuickFilterSelected(): Boolean {
        return chipToday.isChecked || chipThisWeek.isChecked ||
                chipLast30Days.isChecked || chipHighAmounts.isChecked
    }

    private fun applyTodayFilter() {
        val today = Calendar.getInstance()
        val startOfDay = Calendar.getInstance().apply {
            time = today.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endOfDay = Calendar.getInstance().apply {
            time = today.time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        currentFilter = currentFilter.copy(
            datePreset = DatePreset.TODAY,
            startDate = startOfDay.time,
            endDate = endOfDay.time,
            minAmount = null,
            maxAmount = null
        )

        updateFilterIndicator()
        loadExpenses()
    }

    private fun applyThisWeekFilter() {
        val calendar = Calendar.getInstance()

        // Start of week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.time

        // End of week (Sunday)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.time

        currentFilter = currentFilter.copy(
            datePreset = DatePreset.CUSTOM,
            startDate = startOfWeek,
            endDate = endOfWeek,
            minAmount = null,
            maxAmount = null
        )

        updateFilterIndicator()
        loadExpenses()
    }

    private fun applyLast30DaysFilter() {
        val endDate = Date()
        val startDate = Calendar.getInstance().apply {
            time = endDate
            add(Calendar.DAY_OF_YEAR, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        currentFilter = currentFilter.copy(
            datePreset = DatePreset.LAST_30_DAYS,
            startDate = startDate,
            endDate = endDate,
            minAmount = null,
            maxAmount = null
        )

        updateFilterIndicator()
        loadExpenses()
    }

    private fun applyHighAmountsFilter() {
        // Keep current date range but apply amount filter
        currentFilter = currentFilter.copy(
            minAmount = 100.0,
            maxAmount = null
        )

        updateFilterIndicator()
        loadExpenses()
    }

    private fun resetToDefaultFilter() {
        // Reset to show all expenses from current month
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        currentFilter = ExpenseFilter(
            startDate = startOfMonth,
            endDate = endOfMonth,
            datePreset = DatePreset.CUSTOM
        )

        updateFilterIndicator()
        loadExpenses()
    }

    private fun clearOtherQuickFilters(exceptChip: Chip) {
        val allChips = listOf(chipToday, chipThisWeek, chipLast30Days, chipHighAmounts)
        allChips.forEach { chip ->
            if (chip != exceptChip) {
                chip.isChecked = false
            }
        }
    }


    private fun loadPopularSearchTerms() {
        viewModel.getPopularSearchTerms().observe(this) { terms ->
            // You could display these as chips or suggestions when search is empty
            if (terms.isNotEmpty() && etSearchBox.text.isEmpty()) {
                // Show popular terms as suggestions
                showPopularTerms(terms)
            }
        }
    }

    private fun showPopularTerms(terms: List<String>) {
        // Implementation to show popular terms as quick search options
        // This could be chips below the search box or in a dropdown
    }

    private fun showFilterDialog() {
        val filterDialog = ExpenseFilterDialog.newInstance(currentFilter)
        filterDialog.show(supportFragmentManager, "expense_filter")
    }

    override fun onFiltersApplied(filter: ExpenseFilter) {
        currentFilter = filter
        updateFilterIndicator()
        loadExpenses()

        // Hide search suggestions when filter is applied
        layoutSearchSuggestions.visibility = View.GONE
        etSearchBox.clearFocus()
    }

    private fun updateFilterIndicator() {
        if (currentFilter.hasActiveFilters() || anyQuickFilterSelected()) {
            tvFilterIndicator.visibility = View.VISIBLE

            val filterLabels = mutableListOf<String>()

            // Add quick filter labels
            when {
                chipToday.isChecked -> filterLabels.add("Today")
                chipThisWeek.isChecked -> filterLabels.add("This Week")
                chipLast30Days.isChecked -> filterLabels.add("Last 30 Days")
            }

            if (chipHighAmounts.isChecked) {
                filterLabels.add("Over R100")
            }

            // Add other active filters
            if (currentFilter.searchKeyword.isNotBlank()) {
                filterLabels.add("Search")
            }

            if (currentFilter.selectedCategories.isNotEmpty()) {
                filterLabels.add("Categories (${currentFilter.selectedCategories.size})")
            }

            if (currentFilter.minAmount != null || currentFilter.maxAmount != null) {
                if (!chipHighAmounts.isChecked) { // Don't duplicate if high amounts is already shown
                    filterLabels.add("Amount Range")
                }
            }

            tvFilterIndicator.text = if (filterLabels.isNotEmpty()) {
                "Filters: ${filterLabels.joinToString(", ")}"
            } else {
                "All Expenses"
            }

            fabFilter.setImageResource(R.drawable.ic_filter_active)
        } else {
            tvFilterIndicator.visibility = View.GONE
            fabFilter.setImageResource(R.drawable.ic_filter)
        }
    }

    private fun clearSearch() {
        etSearchBox.setText("")
        etSearchBox.clearFocus()
        isSearchActive = false
        layoutSearchSuggestions.visibility = View.GONE
        btnClearSearch.visibility = View.GONE

        // Clear quick filter chips
        chipToday.isChecked = false
        chipThisWeek.isChecked = false
        chipLast30Days.isChecked = false
        chipHighAmounts.isChecked = false

        // Clear search from filter but keep other filters
        currentFilter = currentFilter.copy(
            searchKeyword = "",
            minAmount = null,
            maxAmount = null
        )
        resetToDefaultFilter()
    }

    private fun loadExpenses() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Use search and filter method instead of basic period query
            viewModel.searchAndFilterExpenses(currentFilter).observe(this) { expenses ->
                adapter.updateExpenses(expenses)
                updateTotalAmount(expenses)
                updateSearchResults(expenses.size)

                // Load statistics
                loadExpenseStatistics()
            }
        }
    }

    private fun loadExpenseStatistics() {
        viewModel.getExpenseStatistics(currentFilter).observe(this) { statistics ->
            // You can display these statistics in a summary card
            updateStatisticsDisplay(statistics)
        }
    }

    private fun updateStatisticsDisplay(statistics: ViewModels.ExpenseStatistics) {
        // Update UI with statistics
        // For example, show average amount, most frequent category, etc.
        // This could be in a collapsible card or bottom sheet
    }

    private fun updateSearchResults(count: Int) {
        if (isSearchActive || currentFilter.hasActiveFilters()) {
            tvSearchResults.visibility = View.VISIBLE
            tvSearchResults.text = when {
                count == 0 -> "No expenses found"
                count == 1 -> "1 expense found"
                else -> "$count expenses found"
            }
        } else {
            tvSearchResults.visibility = View.GONE
        }
    }

    private fun updateTotalAmount(expenses: List<Expense>) {
        val total = expenses.sumOf { it.totalAmount }
        val formattedTotal = String.format(Locale.getDefault(), "R%,.2f", total)
        tvTotalExpenses.text = formattedTotal
    }

    override fun onExpenseClicked(expense: Expense) {
        // Handle expense item click if needed
        // For example, show expense details or allow editing
    }

    override fun onDownloadReceiptClicked(expense: Expense) {
        // Handle photo URL from Firestore
        expense.photoUrl?.let { photoUrl ->
            lifecycleScope.launch {
                try {
                    Log.d("ExpenseListActivity", "Attempting to open photo URL: $photoUrl")

                    if (photoUrl.startsWith("https://")) {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(photoUrl), "image/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            startActivity(intent)
                        } catch (activityNotFoundException: ActivityNotFoundException) {
                            val chooserIntent = Intent.createChooser(intent, "Open Receipt with...")
                            try {
                                startActivity(chooserIntent)
                            } catch (e: Exception) {
                                Log.e("ExpenseListActivity", "Failed to open image", e)
                                Toast.makeText(
                                    this@ExpenseListActivity,
                                    "Unable to open receipt. No compatible apps found.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        handleLocalPhotoFile(expense)
                    }
                } catch (e: Exception) {
                    Log.e("ExpenseListActivity", "Error accessing receipt", e)
                    Toast.makeText(
                        this@ExpenseListActivity,
                        "Error accessing receipt: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "No receipt available for this expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLocalPhotoFile(expense: Expense) {
        expense.photoPath?.let { photoPath ->
            val photoFile = File(photoPath)

            if (!photoFile.exists()) {
                Log.e("ExpenseListActivity", "Photo file does not exist: ${photoFile.absolutePath}")
                Toast.makeText(this, "Receipt image file not found", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    photoFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(photoUri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                try {
                    startActivity(intent)
                } catch (activityNotFoundException: ActivityNotFoundException) {
                    val chooserIntent = Intent.createChooser(intent, "Open Receipt with...")
                    try {
                        startActivity(chooserIntent)
                    } catch (e: Exception) {
                        Log.e("ExpenseListActivity", "Failed to open image", e)
                        Toast.makeText(
                            this,
                            "Unable to open receipt. No compatible apps found.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ExpenseListActivity", "Error creating file URI", e)
                Toast.makeText(this, "Error accessing local receipt file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}