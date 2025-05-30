package com.firstproject.prog7313_budgetbuddy

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

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var etSearchKeyword: EditText
    private lateinit var chipGroupDatePresets: ChipGroup
    private lateinit var layoutCustomDateRange: LinearLayout
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var etMinAmount: EditText
    private lateinit var etMaxAmount: EditText
    private lateinit var chipGroupAmountFilters: ChipGroup
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var spinnerSortBy: Spinner
    private lateinit var btnClearFilters: Button
    private lateinit var btnCancel: Button
    private lateinit var btnApplyFilters: Button

    // Data
    private var currentFilter = ExpenseFilter()
    private var categories = listOf<Category>()
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private var fromDate: Calendar? = null
    private var toDate: Calendar? = null

    // Callback interface
    interface FilterApplyListener {
        fun onFiltersApplied(filter: ExpenseFilter)
    }

    companion object {
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
        return inflater.inflate(R.layout.dialog_expense_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase and ViewModel
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(requireActivity())[ViewModels::class.java]

        initializeViews(view)
        setupDatePresets()
        setupAmountFilters()
        setupSortSpinner()
        loadCategories()
        populateCurrentFilter()
        setupListeners()
    }

    private fun initializeViews(view: View) {
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
        chipGroupDatePresets.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = view?.findViewById<Chip>(checkedIds[0])
                val showCustomRange = selectedChip?.id == R.id.chipCustom
                layoutCustomDateRange.visibility = if (showCustomRange) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupAmountFilters() {
        val amountChips = listOf(
            R.id.chipUnder50,
            R.id.chipOver50,
            R.id.chipOver100,
            R.id.chipOver500
        )

        amountChips.forEach { chipId ->
            view?.findViewById<Chip>(chipId)?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Clear other amount chips and set appropriate amounts
                    amountChips.forEach { otherId ->
                        if (otherId != chipId) {
                            view?.findViewById<Chip>(otherId)?.isChecked = false
                        }
                    }

                    when (chipId) {
                        R.id.chipUnder50 -> {
                            etMinAmount.setText("")
                            etMaxAmount.setText("50")
                        }
                        R.id.chipOver50 -> {
                            etMinAmount.setText("50")
                            etMaxAmount.setText("")
                        }
                        R.id.chipOver100 -> {
                            etMinAmount.setText("100")
                            etMaxAmount.setText("")
                        }
                        R.id.chipOver500 -> {
                            etMinAmount.setText("500")
                            etMaxAmount.setText("")
                        }
                    }
                }
            }
        }
    }

    private fun setupSortSpinner() {
        val sortOptions = SortOption.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSortBy.adapter = adapter
    }

    private fun loadCategories() {
        viewModel.getAllCategories().observe(this) { categoryList ->
            categories = categoryList
            setupCategoryChips()
        }
    }

    private fun setupCategoryChips() {
        chipGroupCategories.removeAllViews()

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.categoryName
                isCheckable = true
                tag = category.id

                // Set chip color based on category color
                try {
                    val color = android.graphics.Color.parseColor(category.colour)
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(color)
                } catch (e: Exception) {
                    // Use default color if parsing fails
                }
            }

            chipGroupCategories.addView(chip)
        }
    }

    private fun populateCurrentFilter() {
        // Populate search keyword
        etSearchKeyword.setText(currentFilter.searchKeyword)

        // Populate date preset
        val datePresetChip = when (currentFilter.datePreset) {
            DatePreset.TODAY -> R.id.chipToday
            DatePreset.LAST_7_DAYS -> R.id.chipLast7Days
            DatePreset.LAST_30_DAYS -> R.id.chipLast30Days
            DatePreset.LAST_3_MONTHS -> R.id.chipLast3Months
            else -> R.id.chipCustom
        }
        view?.findViewById<Chip>(datePresetChip)?.isChecked = true

        // Populate custom dates if using custom range
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

        // Populate amount range
        currentFilter.minAmount?.let { etMinAmount.setText(it.toString()) }
        currentFilter.maxAmount?.let { etMaxAmount.setText(it.toString()) }

        // Populate categories
        currentFilter.selectedCategories.forEach { categoryId ->
            for (i in 0 until chipGroupCategories.childCount) {
                val chip = chipGroupCategories.getChildAt(i) as? Chip
                if (chip?.tag == categoryId) {
                    chip.isChecked = true
                    break
                }
            }
        }

        // Populate sort option
        spinnerSortBy.setSelection(currentFilter.sortBy.ordinal)
    }

    private fun setupListeners() {
        // Date picker listeners
        etFromDate.setOnClickListener {
            showDatePicker(true)
        }

        etToDate.setOnClickListener {
            showDatePicker(false)
        }

        // Clear filters
        btnClearFilters.setOnClickListener {
            clearAllFilters()
        }

        // Cancel
        btnCancel.setOnClickListener {
            dismiss()
        }

        // Apply filters
        btnApplyFilters.setOnClickListener {
            applyFilters()
        }
    }

    private fun showDatePicker(isFromDate: Boolean) {
        val calendar = if (isFromDate) {
            fromDate ?: Calendar.getInstance()
        } else {
            toDate ?: Calendar.getInstance()
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                    if (isFromDate) {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    } else {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                }

                if (isFromDate) {
                    fromDate = selectedCalendar
                    etFromDate.setText(dateFormatter.format(selectedCalendar.time))
                } else {
                    toDate = selectedCalendar
                    etToDate.setText(dateFormatter.format(selectedCalendar.time))
                }

                // Automatically switch to custom range when dates are manually set
                view?.findViewById<Chip>(R.id.chipCustom)?.isChecked = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun clearAllFilters() {
        etSearchKeyword.setText("")
        view?.findViewById<Chip>(R.id.chipCustom)?.isChecked = true
        etFromDate.setText("")
        etToDate.setText("")
        etMinAmount.setText("")
        etMaxAmount.setText("")

        // Clear amount filter chips
        chipGroupAmountFilters.clearCheck()

        // Clear category chips
        for (i in 0 until chipGroupCategories.childCount) {
            (chipGroupCategories.getChildAt(i) as? Chip)?.isChecked = false
        }

        spinnerSortBy.setSelection(0)
        fromDate = null
        toDate = null
    }

    private fun applyFilters() {
        // Get selected date preset
        val selectedDatePreset = when (chipGroupDatePresets.checkedChipId) {
            R.id.chipToday -> DatePreset.TODAY
            R.id.chipLast7Days -> DatePreset.LAST_7_DAYS
            R.id.chipLast30Days -> DatePreset.LAST_30_DAYS
            R.id.chipLast3Months -> DatePreset.LAST_3_MONTHS
            else -> DatePreset.CUSTOM
        }

        // Get selected categories
        val selectedCategories = mutableListOf<String>()
        for (i in 0 until chipGroupCategories.childCount) {
            val chip = chipGroupCategories.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                chip.tag?.toString()?.let { selectedCategories.add(it) }
            }
        }

        // Parse amount values
        val minAmount = etMinAmount.text.toString().toDoubleOrNull()
        val maxAmount = etMaxAmount.text.toString().toDoubleOrNull()

        // Get custom dates if using custom range
        val (startDate, endDate) = if (selectedDatePreset == DatePreset.CUSTOM) {
            Pair(fromDate?.time, toDate?.time)
        } else {
            Pair(null, null)
        }

        // Create filter object
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

        // Apply filters through callback
        (parentFragment as? FilterApplyListener)?.onFiltersApplied(filter)
            ?: (activity as? FilterApplyListener)?.onFiltersApplied(filter)

        dismiss()
    }
}