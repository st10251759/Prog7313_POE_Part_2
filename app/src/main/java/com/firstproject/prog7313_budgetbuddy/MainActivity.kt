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

// Import necessary Android, Firebase and Kotlin libraries
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.adapters.HomeCategoryAdapter
import com.firstproject.prog7313_budgetbuddy.data.entities.BudgetGoal
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

//Home Dashboard - Main Activity responsible for displaying budget data, expense categories, and controlling UI elements
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: HomeCategoryAdapter

    // UI Components for displaying budget data and user interaction
    private lateinit var btnAddExpense: Button
    private lateinit var tvTotalBudget: TextView
    private lateinit var tvMinBudget: TextView
    private lateinit var tvMaxBudget: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var tvEditBudgetGoals: TextView
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var rvCategorySpending: RecyclerView

    // Date range for showing data (start and end of current month by default)
    private var startDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1) // Set start to the first day of the month
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private var endDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) // Set end to the last day of the month
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    // Currency formatter for formatting amounts into South African Rand (ZAR)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enabling edge-to-edge layout
        setContentView(R.layout.activity_main)

        // Adjust padding to account for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth for user authentication
        auth = FirebaseAuth.getInstance()

        // If the user is not logged in, redirect to the LoginActivity
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Close the current activity
            return
        }

        // Initialize ViewModel to manage UI-related data in a lifecycle-conscious way
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize UI components
        initializeUI()
        setupListeners()

        // Load budget and spending data
        loadBudgetData()
        loadCategorySpending()
    }

    // Initialize the UI components for the main activity
    private fun initializeUI() {
        btnAddExpense = findViewById(R.id.btnAddExpense)
        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvMinBudget = findViewById(R.id.tvMinBudget)
        tvMaxBudget = findViewById(R.id.tvMaxBudget)
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget)
        tvEditBudgetGoals = findViewById(R.id.tvEditBudgetGoals)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        rvCategorySpending = findViewById(R.id.rvCategorySpending)

        // Set up the adapter for category spending and RecyclerView layout
        adapter = HomeCategoryAdapter(emptyList())
        rvCategorySpending.layoutManager = LinearLayoutManager(this)
        rvCategorySpending.adapter = adapter
        rvCategorySpending.isNestedScrollingEnabled = false
    }

    // Set up listeners for user interactions with buttons and views
    private fun setupListeners() {
        // Button to add a new expense
        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        // Button to edit budget goals
        tvEditBudgetGoals.setOnClickListener {
            startActivity(Intent(this, BudgetGoalsActivity::class.java))
        }

        // Navigation menu item listeners
        findViewById<View>(R.id.navHome).setOnClickListener { }
        findViewById<View>(R.id.navExpenseList).setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }
        findViewById<View>(R.id.navCategorySpending).setOnClickListener {
            startActivity(Intent(this, CategorySpendingActivity::class.java))
        }
        findViewById<View>(R.id.navBudgetGoals).setOnClickListener {
            startActivity(Intent(this, BudgetGoalsActivity::class.java))
        }

        // Period selection buttons
        findViewById<TextView>(R.id.btnMonthly).setOnClickListener { setPeriodToggle(PeriodType.MONTHLY) }
        findViewById<TextView>(R.id.btnWeekly).setOnClickListener { setPeriodToggle(PeriodType.WEEKLY) }
        findViewById<TextView>(R.id.btnDaily).setOnClickListener { setPeriodToggle(PeriodType.DAILY) }
    }

    // Load the budget data for the user (total spent, budget goal)
    private fun loadBudgetData() {
        val userId = auth.currentUser?.uid ?: return // Ensure a valid user is logged in

        // Observe the current budget goal for the user
        viewModel.getCurrentBudgetGoal(userId).observe(this) { budgetGoal ->
            if (budgetGoal == null) {
                // No budget goal set, display default values
                updateBudgetDisplay(null, 0.0)
                updateSpendingDisplay(0.0, null)
                return@observe
            }
            // Load total expenses for the period
            viewModel.getTotalExpensesForPeriod(userId, startDate.time, endDate.time)
                .observe(this) { totalSpent ->
                    val safeTotal = totalSpent ?: 0.0
                    updateBudgetDisplay(budgetGoal, safeTotal)
                    updateSpendingDisplay(safeTotal, budgetGoal)
                }
        }
    }

    // Load the category-wise spending data for the user
    private fun loadCategorySpending() {
        val userId = auth.currentUser?.uid ?: return
        // Observe the category spending data for the period
        viewModel.getCategorySpendingForPeriod(userId, startDate.time, endDate.time)
            .observe(this) { categorySpending ->
                adapter.updateCategories(categorySpending ?: emptyList())
            }
    }

    // Update the budget display based on the user's budget goal and total spent
    private fun updateBudgetDisplay(budgetGoal: BudgetGoal?, totalSpent: Double) {
        if (budgetGoal == null) {
            // Default values when no budget goal is set
            tvTotalBudget.text = "R0.00"
            tvMinBudget.text = "min R0"
            tvMaxBudget.text = "max R0"
            tvRemainingBudget.text = "R0.00 remaining"
            budgetProgressBar.progress = 0
            return
        }
        // Show the current budget and progress
        tvTotalBudget.text = formatCurrency(totalSpent)
        tvMinBudget.text = "min ${formatCurrency(budgetGoal.minGoalAmount)}"
        tvMaxBudget.text = "max ${formatCurrency(budgetGoal.maxGoalAmount)}"
    }

    // Update the spending display (remaining budget and progress bar)
    private fun updateSpendingDisplay(totalSpent: Double, budgetGoal: BudgetGoal?) {
        if (budgetGoal == null) {
            tvRemainingBudget.text = "No budget set"
            budgetProgressBar.progress = 0
            return
        }
        val maxAmount = budgetGoal.maxGoalAmount
        val remaining = maxAmount - totalSpent

        if (remaining >= 0) {
            tvRemainingBudget.text = "${formatCurrency(remaining)} remaining"
        } else {
            // Display if the user is over budget
            tvRemainingBudget.text = "Over budget by ${formatCurrency(-remaining)}"
        }

        // Update progress bar with spending percentage
        val progressPercentage = if (maxAmount > 0) ((totalSpent / maxAmount) * 100).toInt() else 0
        budgetProgressBar.progress = progressPercentage.coerceIn(0, 100)

        // Change color based on whether the user is over budget
        tvRemainingBudget.setTextColor(
            if (remaining < 0) getColor(R.color.coral_pink)
            else getColor(R.color.olivine)
        )
    }

    // Set the period filter (monthly, weekly, or daily) and update data
    private fun setPeriodToggle(periodType: PeriodType) {
        val btnMonthly = findViewById<TextView>(R.id.btnMonthly)
        val btnWeekly  = findViewById<TextView>(R.id.btnWeekly)
        val btnDaily   = findViewById<TextView>(R.id.btnDaily)

        // Reset button backgrounds and text colors
        btnMonthly.background = null; btnWeekly.background = null; btnDaily.background = null
        btnMonthly.setTextColor(getColor(R.color.asparagus))
        btnWeekly.setTextColor(getColor(R.color.asparagus))
        btnDaily.setTextColor(getColor(R.color.asparagus))

        // Update the selected period
        when(periodType) {
            PeriodType.MONTHLY -> {
                btnMonthly.background = getDrawable(R.drawable.period_toggle_selected);
                btnMonthly.setTextColor(getColor(android.R.color.white));
                setMonthlyDateRange()
            }
            PeriodType.WEEKLY  -> {
                btnWeekly.background  = getDrawable(R.drawable.period_toggle_selected);
                btnWeekly.setTextColor(getColor(android.R.color.white));
                setWeeklyDateRange()
            }
            PeriodType.DAILY   -> {
                btnDaily.background   = getDrawable(R.drawable.period_toggle_selected);
                btnDaily.setTextColor(getColor(android.R.color.white));
                setDailyDateRange()
            }
        }
        // Reload data after period change
        loadBudgetData()
        loadCategorySpending()
    }

    // Set date range for monthly view
    private fun setMonthlyDateRange() {
        startDate = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH,1); set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }
        endDate   = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH,getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY,23); set(Calendar.MINUTE,59); set(Calendar.SECOND,59); set(Calendar.MILLISECOND,999) }
    }
    // Set date range for weekly view
    private fun setWeeklyDateRange() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek); cal.set(Calendar.HOUR_OF_DAY,0); cal.set(Calendar.MINUTE,0); cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0)
        startDate = cal.clone() as Calendar
        cal.add(Calendar.DAY_OF_WEEK,6); cal.set(Calendar.HOUR_OF_DAY,23); cal.set(Calendar.MINUTE,59); cal.set(Calendar.SECOND,59); cal.set(Calendar.MILLISECOND,999)
        endDate = cal.clone() as Calendar
    }
    // Set date range for daily view
    private fun setDailyDateRange() {
        val today = Calendar.getInstance()
        startDate = Calendar.getInstance().apply { set(Calendar.YEAR, today.get(Calendar.YEAR)); set(Calendar.MONTH,today.get(Calendar.MONTH)); set(Calendar.DAY_OF_MONTH,today.get(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0) }
        endDate   = Calendar.getInstance().apply { set(Calendar.YEAR, today.get(Calendar.YEAR)); set(Calendar.MONTH,today.get(Calendar.MONTH)); set(Calendar.DAY_OF_MONTH,today.get(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY,23);set(Calendar.MINUTE,59);set(Calendar.SECOND,59);set(Calendar.MILLISECOND,999) }
    }

    // Helper function to format currency
    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("ZAR", "R")
    }

    override fun onResume() {
        super.onResume()
        loadBudgetData()
        loadCategorySpending()
    }

    // Enum for selecting the data period enum class PeriodType { MONTHLY, WEEKLY, DAILY }
    enum class PeriodType { MONTHLY, WEEKLY, DAILY }
}
