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

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.firstproject.prog7313_budgetbuddy.data.models.*
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ExpenseFilterDialog : BottomSheetDialogFragment() {

    // ViewModel and Firebase authentication for retrieving categories, etc.
    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var etSearchKeyword: EditText             // Keyword search input
    private lateinit var chipGroupDatePresets: ChipGroup       // Group of date preset chips (e.g., Today, Last 7 Days)
    private lateinit var layoutCustomDateRange: LinearLayout   // Layout containing custom from/to date inputs
    private lateinit var etFromDate: EditText                  // Custom "From" date input
    private lateinit var etToDate: EditText                    // Custom "To" date input
    private lateinit var etMinAmount: EditText                  // Minimum amount input
    private lateinit var etMaxAmount: EditText                  // Maximum amount input
    private lateinit var chipGroupAmountFilters: ChipGroup      // Group of amount range chips (e.g., Under 50, Over 100)
    private lateinit var chipGroupCategories: ChipGroup         // Group of category filter chips
    private lateinit var spinnerSortBy: Spinner                  // Spinner for choosing sort option
    private lateinit var btnClearFilters: Button                 // Button to clear all filters
    private lateinit var btnCancel: Button                       // Button to cancel and close dialog
    private lateinit var btnApplyFilters: Button                 // Button to apply selected filters

    // Data-backed state
    private var currentFilter = ExpenseFilter()                  // Current filter values to populate dialog
    private var categories = listOf<Category>()                  // List of all categories to display as chips
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) // Formatter for date inputs
    private var fromDate: Calendar? = null                        // Selected "From" date for custom range
    private var toDate: Calendar? = null                          // Selected "To" date for custom range

    // Callback interface to notify host about applied filters
    interface FilterApplyListener {
        fun onFiltersApplied(filter: ExpenseFilter)
    }

    companion object {
        // Factory method to create a new instance of this dialog with an existing filter
        fun newInstance(currentFilter: ExpenseFilter): ExpenseFilterDialog {
            return ExpenseFilterDialog().apply {
                this.currentFilter = currentFilter
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the expense filter dialog
        return inflater.inflate(R.layout.dialog_expense_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and ViewModel for fetching categories and other data
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(requireActivity())[ViewModels::class.java]

        // Bind UI elements to properties
        initializeViews(view)
        // Configure date preset chips to show/hide custom date inputs
        setupDatePresets()
        // Configure amount filter chips to auto-fill min/max amount fields
        setupAmountFilters()
        // Populate the sort-by spinner with available sort options
        setupSortSpinner()
        // Load all categories from ViewModel and create category chips
        loadCategories()
        // Populate UI fields with values from currentFilter (if any)
        populateCurrentFilter()
        // Attach click listeners and event handlers
        setupListeners()
    }

    private fun initializeViews(view: View) {
        // Bind EditText and other UI elements from inflated layout
        etSearchKeyword = view.findViewById(R.id.etSearchKeyword)
        chipGroupDatePresets = view.findViewById(R.id.chipGroupDatePresets)
        layoutCustomDateRange = view.findViewById(R.id.layoutCustomDateRange)
        etFromDate = view.findViewById(R.id.etFromDate)
        etToDate = view.findViewById(R.id.etToDate)
        etMinAmount = view.findViewById(R.id.etMinAmount)
        etMaxAmount = view.findViewById(R.id.etMaxAmount)
        chipGroupAmountFilters = view.findViewById(R.id.chipGroupAmountFilters)
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)
        spinnerSortBy = view.findViewById(R.id.spinnerSortBy)
        btnClearFilters = view.findViewById(R.id.btnClearFilters)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters)
    }

    private fun setupDatePresets() {
        // Listen for changes in which date preset chip is selected
        chipGroupDatePresets.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                // If a preset is selected, find that chip
                val selectedChip = view?.findViewById<Chip>(checkedIds[0])
                // Show custom date range inputs only if the "Custom" chip is checked
                val showCustomRange = selectedChip?.id == R.id.chipCustom
                layoutCustomDateRange.visibility = if (showCustomRange) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupAmountFilters() {
        // IDs of the amount filter chips to iterate over
        val amountChips = listOf(
            R.id.chipUnder50,
            R.id.chipOver50,
            R.id.chipOver100,
            R.id.chipOver500
        )

        amountChips.forEach { chipId ->
            // For each chip, add a listener to respond when it is checked
            view?.findViewById<Chip>(chipId)?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // When one chip is checked, uncheck all other amount chips
                    amountChips.forEach { otherId ->
                        if (otherId != chipId) {
                            view?.findViewById<Chip>(otherId)?.isChecked = false
                        }
                    }

                    // Based on which chip is selected, set min/max amount fields appropriately
                    when (chipId) {
                        R.id.chipUnder50 -> {
                            etMinAmount.setText("")    // No minimum
                            etMaxAmount.setText("50")  // Max = 50
                        }
                        R.id.chipOver50 -> {
                            etMinAmount.setText("50")  // Min = 50
                            etMaxAmount.setText("")    // No maximum
                        }
                        R.id.chipOver100 -> {
                            etMinAmount.setText("100") // Min = 100
                            etMaxAmount.setText("")    // No maximum
                        }
                        R.id.chipOver500 -> {
                            etMinAmount.setText("500") // Min = 500
                            etMaxAmount.setText("")    // No maximum
                        }
                    }
                }
            }
        }
    }

    private fun setupSortSpinner() {
        // Get display names for all sort options
        val sortOptions = SortOption.values().map { it.displayName }
        // Create an ArrayAdapter for the spinner with those sort option names
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSortBy.adapter = adapter  // Attach adapter to spinner
    }

    private fun loadCategories() {
        // Observe LiveData of category list from ViewModel
        viewModel.getAllCategories().observe(this) { categoryList ->
            categories = categoryList  // Store retrieved categories
            setupCategoryChips()       // Create chips for each category
        }
    }

    private fun setupCategoryChips() {
        // Remove any existing chips before adding new ones
        chipGroupCategories.removeAllViews()

        categories.forEach { category ->
            // Create a new Chip for each category
            val chip = Chip(requireContext()).apply {
                text = category.categoryName   // Display category name
                isCheckable = true             // Allow this chip to be checked/unchecked
                tag = category.id              // Tag holds the category ID

                // Attempt to set chip background color based on category’s color string
                try {
                    val color = android.graphics.Color.parseColor(category.colour)
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(color)
                } catch (e: Exception) {
                    // If parsing fails, leave default chip color
                }
            }
            chipGroupCategories.addView(chip)  // Add chip to the chip group
        }
    }

    private fun populateCurrentFilter() {
        // Populate search keyword input with previously set value
        etSearchKeyword.setText(currentFilter.searchKeyword)

        // Determine which date preset chip corresponds to the current filter
        val datePresetChip = when (currentFilter.datePreset) {
            DatePreset.TODAY -> R.id.chipToday
            DatePreset.LAST_7_DAYS -> R.id.chipLast7Days
            DatePreset.LAST_30_DAYS -> R.id.chipLast30Days
            DatePreset.LAST_3_MONTHS -> R.id.chipLast3Months
            else -> R.id.chipCustom  // If not a predefined preset, use "Custom"
        }
        // Check the appropriate chip
        view?.findViewById<Chip>(datePresetChip)?.isChecked = true

        // If using a custom date range, populate the From/To fields
        if (currentFilter.datePreset == DatePreset.CUSTOM) {
            currentFilter.startDate?.let { date ->
                fromDate = Calendar.getInstance().apply { time = date }
                etFromDate.setText(dateFormatter.format(date))
            }
            currentFilter.endDate?.let { date ->
                toDate = Calendar.getInstance().apply { time = date }
                etToDate.setText(dateFormatter.format(date))
            }
        }

        // Populate the min/max amount fields if they exist in the filter
        currentFilter.minAmount?.let { etMinAmount.setText(it.toString()) }
        currentFilter.maxAmount?.let { etMaxAmount.setText(it.toString()) }

        // Check the category chips corresponding to selected category IDs
        currentFilter.selectedCategories.forEach { categoryId ->
            for (i in 0 until chipGroupCategories.childCount) {
                val chip = chipGroupCategories.getChildAt(i) as? Chip
                if (chip?.tag == categoryId) {
                    chip.isChecked = true
                    break
                }
            }
        }

        // Set spinner selection to saved sort-by option
        spinnerSortBy.setSelection(currentFilter.sortBy.ordinal)
    }

    private fun setupListeners() {
        // When "From Date" EditText is clicked, show date picker for start date
        etFromDate.setOnClickListener {
            showDatePicker(true)
        }

        // When "To Date" EditText is clicked, show date picker for end date
        etToDate.setOnClickListener {
            showDatePicker(false)
        }

        // Clear all filters to defaults when clear button is clicked
        btnClearFilters.setOnClickListener {
            clearAllFilters()
        }

        // Dismiss dialog without applying when cancel button is clicked
        btnCancel.setOnClickListener {
            dismiss()
        }

        // Apply all selected filters when apply button is clicked
        btnApplyFilters.setOnClickListener {
            applyFilters()
        }
    }

    private fun showDatePicker(isFromDate: Boolean) {
        // Determine the initial date to show in the date picker
        val calendar = if (isFromDate) {
            fromDate ?: Calendar.getInstance()
        } else {
            toDate ?: Calendar.getInstance()
        }

        // Show a DatePickerDialog to let user pick a date
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // When a date is selected, create a Calendar for that date
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                    if (isFromDate) {
                        // Set time to start of day if selecting "From Date"
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    } else {
                        // Set time to end of day if selecting "To Date"
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                }

                // Update either fromDate or toDate based on which field was clicked
                if (isFromDate) {
                    fromDate = selectedCalendar
                    etFromDate.setText(dateFormatter.format(selectedCalendar.time))
                } else {
                    toDate = selectedCalendar
                    etToDate.setText(dateFormatter.format(selectedCalendar.time))
                }

                // Automatically switch to "Custom" date preset chip if a manual date is set
                view?.findViewById<Chip>(R.id.chipCustom)?.isChecked = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun clearAllFilters() {
        // Reset search keyword to empty
        etSearchKeyword.setText("")
        // Check the "Custom" date preset so the custom layout is visible
        view?.findViewById<Chip>(R.id.chipCustom)?.isChecked = true
        // Clear custom date inputs
        etFromDate.setText("")
        etToDate.setText("")
        // Clear min/max amount fields
        etMinAmount.setText("")
        etMaxAmount.setText("")

        // Uncheck any selected amount filter chip
        chipGroupAmountFilters.clearCheck()

        // Uncheck all category chips
        for (i in 0 until chipGroupCategories.childCount) {
            (chipGroupCategories.getChildAt(i) as? Chip)?.isChecked = false
        }

        // Reset sort spinner to first option
        spinnerSortBy.setSelection(0)
        // Clear saved from/to dates
        fromDate = null
        toDate = null
    }

    private fun applyFilters() {
        // Determine which date preset is selected based on chip checked ID
        val selectedDatePreset = when (chipGroupDatePresets.checkedChipId) {
            R.id.chipToday -> DatePreset.TODAY
            R.id.chipLast7Days -> DatePreset.LAST_7_DAYS
            R.id.chipLast30Days -> DatePreset.LAST_30_DAYS
            R.id.chipLast3Months -> DatePreset.LAST_3_MONTHS
            else -> DatePreset.CUSTOM
        }

        // Collect IDs of all checked category chips
        val selectedCategories = mutableListOf<String>()
        for (i in 0 until chipGroupCategories.childCount) {
            val chip = chipGroupCategories.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                chip.tag?.toString()?.let { selectedCategories.add(it) }
            }
        }

        // Parse min and max amount inputs to Double or null if empty/invalid
        val minAmount = etMinAmount.text.toString().toDoubleOrNull()
        val maxAmount = etMaxAmount.text.toString().toDoubleOrNull()

        // If using custom date preset, get actual Date objects; otherwise keep null
        val (startDate, endDate) = if (selectedDatePreset == DatePreset.CUSTOM) {
            Pair(fromDate?.time, toDate?.time)
        } else {
            Pair(null, null)
        }

        // Build an ExpenseFilter object using all selected filter criteria
        val filter = ExpenseFilter(
            searchKeyword = etSearchKeyword.text.toString().trim(),
            startDate = startDate,
            endDate = endDate,
            minAmount = minAmount,
            maxAmount = maxAmount,
            selectedCategories = selectedCategories,
            sortBy = SortOption.values()[spinnerSortBy.selectedItemPosition],
            datePreset = selectedDatePreset
        )

        // Invoke callback on parent fragment or activity to apply filters and dismiss dialog
        (parentFragment as? FilterApplyListener)?.onFiltersApplied(filter)
            ?: (activity as? FilterApplyListener)?.onFiltersApplied(filter)

        dismiss() // Close this dialog
    }
}
