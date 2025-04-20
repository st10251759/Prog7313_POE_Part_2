package com.firstproject.prog7313_budgetbuddy

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

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: HomeCategoryAdapter

    // UI Components
    private lateinit var btnAddExpense: Button
    private lateinit var tvTotalBudget: TextView
    private lateinit var tvMinBudget: TextView
    private lateinit var tvMaxBudget: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var tvEditBudgetGoals: TextView
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var rvCategorySpending: RecyclerView

    // Date range for showing data
    private var startDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private var endDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    // Currency formatter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in, if not, go to LoginActivity
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize UI components
        initializeUI()
        setupListeners()

        // Load data
        loadBudgetData()
        loadCategorySpending()
    }

    private fun initializeUI() {
        // Find views
        btnAddExpense = findViewById(R.id.btnAddExpense)
        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvMinBudget = findViewById(R.id.tvMinBudget)
        tvMaxBudget = findViewById(R.id.tvMaxBudget)
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget)
        tvEditBudgetGoals = findViewById(R.id.tvEditBudgetGoals)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        rvCategorySpending = findViewById(R.id.rvCategorySpending)

        // Setup RecyclerView
        adapter = HomeCategoryAdapter(emptyList())
        rvCategorySpending.layoutManager = LinearLayoutManager(this)
        rvCategorySpending.adapter = adapter
        rvCategorySpending.isNestedScrollingEnabled = false
    }

    private fun setupListeners() {
        // Add Expense button
        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        // Edit Budget Goals
        tvEditBudgetGoals.setOnClickListener {
            startActivity(Intent(this, BudgetGoalsActivity::class.java))
        }

        // Bottom Navigation
        findViewById<View>(R.id.navHome).setOnClickListener {
            // Already on home, do nothing
        }

        findViewById<View>(R.id.navExpenseList).setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        findViewById<View>(R.id.navCategorySpending).setOnClickListener {
            startActivity(Intent(this, CategorySpendingActivity::class.java))
        }

        findViewById<View>(R.id.navBudgetGoals).setOnClickListener {
            startActivity(Intent(this, BudgetGoalsActivity::class.java))
        }

        // Period toggles
        findViewById<TextView>(R.id.btnMonthly).setOnClickListener {
            setPeriodToggle(PeriodType.MONTHLY)
        }

        findViewById<TextView>(R.id.btnWeekly).setOnClickListener {
            setPeriodToggle(PeriodType.WEEKLY)
        }

        findViewById<TextView>(R.id.btnDaily).setOnClickListener {
            setPeriodToggle(PeriodType.DAILY)
        }
    }

    private fun loadBudgetData() {
        val userId = auth.currentUser?.uid ?: return

        viewModel.getCurrentBudgetGoal(userId).observe(this) { budgetGoal ->
            if (budgetGoal == null) {
                updateBudgetDisplay(null, 0.0)
                updateSpendingDisplay(0.0, null)
                return@observe
            }

            viewModel.getTotalExpensesForPeriod(userId, startDate.time, endDate.time).observe(this) { totalSpent ->
                val safeTotal = totalSpent ?: 0.0
                updateBudgetDisplay(budgetGoal, safeTotal)
                updateSpendingDisplay(safeTotal, budgetGoal)
            }
        }
    }



    private fun loadCategorySpending() {
        val userId = auth.currentUser?.uid ?: return

        viewModel.getCategorySpendingForPeriod(userId, startDate.time, endDate.time).observe(this) { categorySpending ->
            if (categorySpending != null) {
                adapter.updateCategories(categorySpending)
            } else {
                adapter.updateCategories(emptyList()) // fallback to empty list
            }
        }
    }


    private fun updateBudgetDisplay(budgetGoal: BudgetGoal?, totalSpent: Double) {
        if (budgetGoal == null) {
            // No budget goal set, show defaults
            tvTotalBudget.text = "R0.00"
            tvMinBudget.text = "min R0"
            tvMaxBudget.text = "max R0"
            tvRemainingBudget.text = "R0.00 remaining"
            budgetProgressBar.progress = 0
            return
        }

        // Display total spending in tvTotalBudget
        tvTotalBudget.text = formatCurrency(totalSpent)

        // Format min/max budget numbers
        tvMinBudget.text = "min ${formatCurrency(budgetGoal.minGoalAmount)}"
        tvMaxBudget.text = "max ${formatCurrency(budgetGoal.maxGoalAmount)}"
    }

    private fun updateSpendingDisplay(totalSpent: Double, budgetGoal: BudgetGoal?) {
        if (budgetGoal == null) {
            tvRemainingBudget.text = "No budget set"
            budgetProgressBar.progress = 0
            return
        }

        val maxAmount = budgetGoal.maxGoalAmount
        val remaining = maxAmount - totalSpent

        // Update remaining amount
        tvRemainingBudget.text = "${formatCurrency(remaining)} remaining"

        // Update progress bar
        val progressPercentage = if (maxAmount > 0) ((totalSpent / maxAmount) * 100).toInt() else 0
        budgetProgressBar.progress = progressPercentage.coerceIn(0, 100)

        // Change color if exceeding budget
        if (totalSpent > maxAmount) {
            tvRemainingBudget.setTextColor(getColor(R.color.coral_pink))
        } else {
            tvRemainingBudget.setTextColor(getColor(R.color.olivine))
        }
    }

    private fun setPeriodToggle(periodType: PeriodType) {
        // Update UI for selected period
        val btnMonthly = findViewById<TextView>(R.id.btnMonthly)
        val btnWeekly = findViewById<TextView>(R.id.btnWeekly)
        val btnDaily = findViewById<TextView>(R.id.btnDaily)

        btnMonthly.background = null
        btnWeekly.background = null
        btnDaily.background = null

        btnMonthly.setTextColor(getColor(R.color.asparagus))
        btnWeekly.setTextColor(getColor(R.color.asparagus))
        btnDaily.setTextColor(getColor(R.color.asparagus))

        // Highlight the selected period
        when (periodType) {
            PeriodType.MONTHLY -> {
                btnMonthly.background = getDrawable(R.drawable.period_toggle_selected)
                btnMonthly.setTextColor(getColor(android.R.color.white))
                setMonthlyDateRange()
            }
            PeriodType.WEEKLY -> {
                btnWeekly.background = getDrawable(R.drawable.period_toggle_selected)
                btnWeekly.setTextColor(getColor(android.R.color.white))
                setWeeklyDateRange()
            }
            PeriodType.DAILY -> {
                btnDaily.background = getDrawable(R.drawable.period_toggle_selected)
                btnDaily.setTextColor(getColor(android.R.color.white))
                setDailyDateRange()
            }
        }

        // Reload data with new date range
        loadBudgetData()
        loadCategorySpending()
    }

    private fun setMonthlyDateRange() {
        startDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        endDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    private fun setWeeklyDateRange() {
        val calendar = Calendar.getInstance()

        // Find first day of week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startDate = calendar.clone() as Calendar

        // Find last day of week
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        endDate = calendar.clone() as Calendar
    }

    private fun setDailyDateRange() {
        val today = Calendar.getInstance()

        startDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, today.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        endDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, today.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("ZAR", "R")
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to this screen
        loadBudgetData()
        loadCategorySpending()
    }

    enum class PeriodType {
        MONTHLY, WEEKLY, DAILY
    }
}