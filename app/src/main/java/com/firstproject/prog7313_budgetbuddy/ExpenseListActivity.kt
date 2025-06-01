package com.firstproject.prog7313_budgetbuddy

/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 3
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: How to Filter List View by Multiple Fields Android Studio Tutorial | Multi Select Filter Buttons
 Author: Code With Cal
 Date Published:  24 October 2020
 Date Accessed: 30 May 2025
 Code Version: N/A
 Availability: https://www.youtube.com/watch?app=desktop&v=liGwWbR-2D8

  --------------------------------Code Attribution----------------------------------
*/

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

    // Variables for Firebase Auth, ViewModel, and the RecyclerView adapter
    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ExpenseListAdapter
    private lateinit var btnThemeToggle: ImageButton

    // UI Components from the layout
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

    // State for search and filter
    private var currentFilter = ExpenseFilter()         // Holds the currently applied filter parameters
    private var isSearchActive = false                  // Tracks if a search query is active
    private val searchSuggestions = mutableListOf<String>() // List of auto-complete suggestions for search

    // Quick filter chips
    private lateinit var chipToday: Chip
    private lateinit var chipThisWeek: Chip
    private lateinit var chipLast30Days: Chip
    private lateinit var chipHighAmounts: Chip

    // Formatter for displaying dates in MM/dd/yyyy
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Extend content under status and navigation bars
        setContentView(R.layout.activity_expense_list)

        // Adjust padding to avoid system bars overlapping content
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth to access current user
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel for retrieving expenses, statistics, categories, etc.
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Bind UI components and configure RecyclerView adapter
        initializeUI()
        // Attach click and input listeners for buttons, search field, etc.
        setupListeners()
        // Set up text‐watcher for search box to handle dynamic queries & suggestions
        setupSearchFunctionality()

        // Load the initial list of expenses according to default filter (e.g., current month)
        loadExpenses()
        // Load popular past search terms to show as suggestions when search is empty
        loadPopularSearchTerms()
    }

    private fun initializeUI() {
        // Bind each UI component from the layout XML
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

        // Initialize quick filter chips (e.g., Today, This Week)
        chipToday = findViewById(R.id.chipToday)
        chipThisWeek = findViewById(R.id.chipThisWeek)
        chipLast30Days = findViewById(R.id.chipLast30Days)
        chipHighAmounts = findViewById(R.id.chipHighAmounts)

        // Create adapter with an empty list; 'this' implements ExpenseClickListener
        adapter = ExpenseListAdapter(emptyList(), this)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter

        // Set up theme toggle functionality inherited from BaseActivity
        setupThemeToggle(btnThemeToggle)
        // Show or hide filter indicator based on whether any filters are active
        updateFilterIndicator()
        // Configure behavior of the quick filter chips
        setupQuickFilters()
    }

    private fun setupListeners() {
        // Handle Back button to close the activity
        btnBack.setOnClickListener {
            finish()
        }

        // Show the full filter dialog when filter icon is tapped
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        // Floating Action Button also opens the filter dialog
        fabFilter.setOnClickListener {
            showFilterDialog()
        }

        // Clear the search query when clear icon is tapped
        btnClearSearch.setOnClickListener {
            clearSearch()
        }
    }

    private fun setupSearchFunctionality() {
        // Add a TextWatcher to the search EditText for dynamic search feedback
        etSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()

                if (query.isNotEmpty()) {
                    // If the user has typed something, mark search as active
                    isSearchActive = true
                    btnClearSearch.visibility = View.VISIBLE

                    // Update current filter’s search keyword
                    currentFilter = currentFilter.copy(searchKeyword = query)
                    loadExpenses() // Reload expenses list with updated filter

                    // If at least two characters, fetch suggestions from ViewModel
                    if (query.length >= 2) {
                        loadSearchSuggestions(query)
                    }

                    // Save this query to “recent searches” in ViewModel storage
                    viewModel.saveSearchQuery(query)
                } else {
                    // If search box is emptied, disable search mode and hide suggestions
                    isSearchActive = false
                    btnClearSearch.visibility = View.GONE
                    layoutSearchSuggestions.visibility = View.GONE

                    // Clear the searchKeyword in the filter and reload full expenses list
                    currentFilter = currentFilter.copy(searchKeyword = "")
                    loadExpenses()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        })

        // When the search box gains focus, show suggestions if there is text
        etSearchBox.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && etSearchBox.text.isNotEmpty()) {
                loadSearchSuggestions(etSearchBox.text.toString())
            } else {
                // Otherwise hide the suggestions layout
                layoutSearchSuggestions.visibility = View.GONE
            }
        }
    }

    private fun loadSearchSuggestions(query: String) {
        // Fetch matching suggestions from ViewModel (e.g., recent queries or popular terms)
        viewModel.getSearchSuggestions(query).observe(this) { suggestions ->
            if (suggestions.isNotEmpty() && etSearchBox.hasFocus()) {
                // Show suggestions if there are results and the search box is focused
                showSearchSuggestions(suggestions)
            } else {
                // Hide suggestions if no data or search box not focused
                layoutSearchSuggestions.visibility = View.GONE
            }
        }
    }

    private fun showSearchSuggestions(suggestions: List<String>) {
        // Clear any existing suggestion views
        layoutSearchSuggestions.removeAllViews()

        suggestions.forEach { suggestion ->
            // Create a TextView for each suggestion entry
            val suggestionView = TextView(this).apply {
                text = suggestion
                setPadding(32, 24, 32, 24)                    // Add padding around text
                setTextColor(getColor(R.color.asparagus))     // Use theme’s accent color
                textSize = 14f
                background = getDrawable(R.drawable.suggestion_item_background)

                setOnClickListener {
                    // When user taps a suggestion, populate search box, clear focus, hide suggestions
                    etSearchBox.setText(suggestion)
                    etSearchBox.clearFocus()
                    layoutSearchSuggestions.visibility = View.GONE
                }
            }
            layoutSearchSuggestions.addView(suggestionView)
        }

        // Make the suggestions container visible
        layoutSearchSuggestions.visibility = View.VISIBLE
    }

    // Configure quick "chips" for common filters: Today, This Week, Last 30 Days, High Amounts
    private fun setupQuickFilters() {
        // “Today” chip behavior
        chipToday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherQuickFilters(chipToday)
                applyTodayFilter()
            } else if (!anyQuickFilterSelected()) {
                // If no quick filter is selected, revert to default
                resetToDefaultFilter()
            }
        }

        // “This Week” chip behavior
        chipThisWeek.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherQuickFilters(chipThisWeek)
                applyThisWeekFilter()
            } else if (!anyQuickFilterSelected()) {
                resetToDefaultFilter()
            }
        }

        // “Last 30 Days” chip behavior
        chipLast30Days.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherQuickFilters(chipLast30Days)
                applyLast30DaysFilter()
            } else if (!anyQuickFilterSelected()) {
                resetToDefaultFilter()
            }
        }

        // “High Amounts” (Over R100) chip behavior
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
        // Return true if any of the quick filter chips is currently checked
        return chipToday.isChecked || chipThisWeek.isChecked ||
                chipLast30Days.isChecked || chipHighAmounts.isChecked
    }

    private fun applyTodayFilter() {
        // Build start of today at 00:00:00.000
        val today = Calendar.getInstance()
        val startOfDay = Calendar.getInstance().apply {
            time = today.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Build end of today at 23:59:59.999
        val endOfDay = Calendar.getInstance().apply {
            time = today.time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        // Update filter to include only today’s date range, clear amount filters
        currentFilter = currentFilter.copy(
            datePreset = DatePreset.TODAY,
            startDate = startOfDay.time,
            endDate = endOfDay.time,
            minAmount = null,
            maxAmount = null
        )

        updateFilterIndicator() // Refresh visible filter summary
        loadExpenses()          // Reload expenses with new filter
    }

    private fun applyThisWeekFilter() {
        val calendar = Calendar.getInstance()

        // Set calendar to the start of the current week (Monday at 00:00:00)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.time

        // Move calendar to end of this week (Sunday at 23:59:59.999)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.time

        // Update filter to custom date range for the week, clear amount filters
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
        // Build endDate as today
        val endDate = Date()
        // Build startDate as 30 days before today at 00:00:00
        val startDate = Calendar.getInstance().apply {
            time = endDate
            add(Calendar.DAY_OF_YEAR, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Update filter to last 30 days, clear amount filters
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
        // Keep existing date range, but apply minimum amount = 100 (no maximum)
        currentFilter = currentFilter.copy(
            minAmount = 100.0,
            maxAmount = null
        )

        updateFilterIndicator()
        loadExpenses()
    }

    private fun resetToDefaultFilter() {
        // Define start of current month at 00:00:00
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Define end of current month at 23:59:59.999
        val endOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        // Reset filter to show all expenses from the current month, no category or amount filtering
        currentFilter = ExpenseFilter(
            startDate = startOfMonth,
            endDate = endOfMonth,
            datePreset = DatePreset.CUSTOM
        )

        updateFilterIndicator()
        loadExpenses()
    }

    private fun clearOtherQuickFilters(exceptChip: Chip) {
        // Given a chip to keep checked, uncheck all other quick filter chips
        val allChips = listOf(chipToday, chipThisWeek, chipLast30Days, chipHighAmounts)
        allChips.forEach { chip ->
            if (chip != exceptChip) {
                chip.isChecked = false
            }
        }
    }

    private fun loadPopularSearchTerms() {
        // Observe popular search terms from ViewModel (e.g., most frequent queries)
        viewModel.getPopularSearchTerms().observe(this) { terms ->
            // If there are popular terms and user hasn’t typed anything, show them
            if (terms.isNotEmpty() && etSearchBox.text.isEmpty()) {
                showPopularTerms(terms)
            }
        }
    }

    private fun showPopularTerms(terms: List<String>) {
        // Implementation placeholder: display these popular terms as chips or suggestions
    }

    private fun showFilterDialog() {
        // Instantiate and show the ExpenseFilterDialog, passing in the currentFilter
        val filterDialog = ExpenseFilterDialog.newInstance(currentFilter)
        filterDialog.show(supportFragmentManager, "expense_filter")
    }

    // Callback from ExpenseFilterDialog when user applies filters
    override fun onFiltersApplied(filter: ExpenseFilter) {
        currentFilter = filter           // Update local filter state
        updateFilterIndicator()          // Refresh the filter summary in UI
        loadExpenses()                   // Reload expenses with new filter

        // Hide any visible search suggestions and clear focus from search box
        layoutSearchSuggestions.visibility = View.GONE
        etSearchBox.clearFocus()
    }

    private fun updateFilterIndicator() {
        // Determine whether some filter is active (search text, amount, category, or quick filter)
        if (currentFilter.hasActiveFilters() || anyQuickFilterSelected()) {
            tvFilterIndicator.visibility = View.VISIBLE

            val filterLabels = mutableListOf<String>()

            // Add labels for active quick filters
            when {
                chipToday.isChecked -> filterLabels.add("Today")
                chipThisWeek.isChecked -> filterLabels.add("This Week")
                chipLast30Days.isChecked -> filterLabels.add("Last 30 Days")
            }
            if (chipHighAmounts.isChecked) {
                filterLabels.add("Over R100")
            }

            // Add a label if a search keyword exists
            if (currentFilter.searchKeyword.isNotBlank()) {
                filterLabels.add("Search")
            }

            // Add a label if categories are selected
            if (currentFilter.selectedCategories.isNotEmpty()) {
                filterLabels.add("Categories (${currentFilter.selectedCategories.size})")
            }

            // Add a label if amount range is set (and “High Amounts” chip is not already shown)
            if (currentFilter.minAmount != null || currentFilter.maxAmount != null) {
                if (!chipHighAmounts.isChecked) {
                    filterLabels.add("Amount Range")
                }
            }

            // Compose text for filter summary
            tvFilterIndicator.text = if (filterLabels.isNotEmpty()) {
                "Filters: ${filterLabels.joinToString(", ")}"
            } else {
                "All Expenses"
            }

            // Change the FAB icon to indicate active filter
            fabFilter.setImageResource(R.drawable.ic_filter_active)
        } else {
            // No filters: hide the filter indicator and show neutral filter icon
            tvFilterIndicator.visibility = View.GONE
            fabFilter.setImageResource(R.drawable.ic_filter)
        }
    }

    private fun clearSearch() {
        // Clear the search box text and focus
        etSearchBox.setText("")
        etSearchBox.clearFocus()
        isSearchActive = false
        layoutSearchSuggestions.visibility = View.GONE
        btnClearSearch.visibility = View.GONE

        // Uncheck all quick filter chips
        chipToday.isChecked = false
        chipThisWeek.isChecked = false
        chipLast30Days.isChecked = false
        chipHighAmounts.isChecked = false

        // Remove any search or amount constraints from current filter, but keep date
        currentFilter = currentFilter.copy(
            searchKeyword = "",
            minAmount = null,
            maxAmount = null
        )
        // Reset to default (current month) filter
        resetToDefaultFilter()
    }

    private fun loadExpenses() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Fetch filtered & searched expenses from ViewModel and observe changes
            viewModel.searchAndFilterExpenses(currentFilter).observe(this) { expenses ->
                adapter.updateExpenses(expenses)    // Update RecyclerView’s data
                updateTotalAmount(expenses)         // Update total sum display
                updateSearchResults(expenses.size)  // Show how many results found

                // Also load summary statistics asynchronously
                loadExpenseStatistics()
            }
        }
    }

    private fun loadExpenseStatistics() {
        // Fetch computed statistics (e.g., averages, breakdowns) from ViewModel
        viewModel.getExpenseStatistics(currentFilter).observe(this) { statistics ->
            // Display these stats in a summary UI component or bottom sheet
            updateStatisticsDisplay(statistics)
        }
    }

    private fun updateStatisticsDisplay(statistics: ViewModels.ExpenseStatistics) {
        // TODO: Update UI with the statistics (e.g., average, top category)
        // This might populate a card or collapse/expand panel
    }

    private fun updateSearchResults(count: Int) {
        // If a search query is active or filters are applied, show results info
        if (isSearchActive || currentFilter.hasActiveFilters()) {
            tvSearchResults.visibility = View.VISIBLE
            tvSearchResults.text = when {
                count == 0 -> "No expenses found"
                count == 1 -> "1 expense found"
                else -> "$count expenses found"
            }
        } else {
            // Hide the “X expenses found” text when no search/filter is in effect
            tvSearchResults.visibility = View.GONE
        }
    }

    private fun updateTotalAmount(expenses: List<Expense>) {
        // Sum up totalAmount for all displayed expenses
        val total = expenses.sumOf { it.totalAmount }
        // Format as currency string “R1,234.56”
        val formattedTotal = String.format(Locale.getDefault(), "R%,.2f", total)
        tvTotalExpenses.text = formattedTotal
    }

    override fun onExpenseClicked(expense: Expense) {
        // Handle clicks on an individual expense (e.g., open details or edit screen)
    }

    override fun onDownloadReceiptClicked(expense: Expense) {
        // Handle “download receipt” action for an expense:
        // First attempt to open a remote URL if it starts with “https://”
        expense.photoUrl?.let { photoUrl ->
            lifecycleScope.launch {
                try {
                    Log.d("ExpenseListActivity", "Attempting to open photo URL: $photoUrl")

                    if (photoUrl.startsWith("https://")) {
                        // Create intent to view an image from a web URL
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(photoUrl), "image/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            startActivity(intent)
                        } catch (activityNotFoundException: ActivityNotFoundException) {
                            // If no app to handle it, offer a chooser
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
                        // If photoUrl is not an HTTP URL, attempt to open local file
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
            // If no photoUrl exists, show a toast indicating no receipt available
            Toast.makeText(this, "No receipt available for this expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLocalPhotoFile(expense: Expense) {
        // If the expense has a local file path for the receipt
        expense.photoPath?.let { photoPath ->
            val photoFile = File(photoPath)

            if (!photoFile.exists()) {
                Log.e("ExpenseListActivity", "Photo file does not exist: ${photoFile.absolutePath}")
                Toast.makeText(this, "Receipt image file not found", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                // Convert the local file into a content:// URI via FileProvider
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    photoFile
                )

                // Create intent to view the image
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(photoUri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                try {
                    startActivity(intent)
                } catch (activityNotFoundException: ActivityNotFoundException) {
                    // If no direct viewer, show a chooser dialog
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

