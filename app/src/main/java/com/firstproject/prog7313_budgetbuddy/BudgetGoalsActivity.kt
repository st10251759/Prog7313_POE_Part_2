package com.firstproject.prog7313_budgetbuddy

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.firstproject.prog7313_budgetbuddy.data.entities.BudgetGoal
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BudgetGoalsActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var tvMinimumBudget: TextView
    private lateinit var tvMaximumBudget: TextView
    private lateinit var tvKeypadAmount: TextView
    private lateinit var btnSaveBudget: Button
    private lateinit var btnCancel: Button
    private var editingMinimum: Boolean = true

    // Keypad buttons
    private lateinit var numButtons: List<Button>
    private lateinit var btnDot: Button
    private lateinit var btnDelete: Button

    // Data
    private var currentAmount = "0.00"
    private var minGoalAmount = 0.00
    private var maxGoalAmount = 0.00
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

    // Date formatter
    private val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget_goals)

        // Set up window insets
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

        // Default to editing minimum budget
        editingMinimum = true
        highlightActiveField()

        // Load existing budget goal if any
        loadCurrentBudgetGoal()
    }

    private fun initializeUI() {
        btnBack = findViewById(R.id.btnBack)
        tvMinimumBudget = findViewById(R.id.tvMinimumBudget)
        tvMaximumBudget = findViewById(R.id.tvMaximumBudget)
        tvKeypadAmount = findViewById(R.id.tvKeypadAmount)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        btnCancel = findViewById(R.id.btnCancel)

        // Set the click listeners on tvMinimumBudget and tvMaximumBudget to enable editing
        tvMinimumBudget.setOnClickListener {
            editingMinimum = true
            highlightActiveField()
            currentAmount = if (minGoalAmount > 0) {
                String.format(Locale.getDefault(), "%.2f", minGoalAmount)
            } else {
                "0.00"
            }
            updateAmountDisplay()
        }

        tvMaximumBudget.setOnClickListener {
            editingMinimum = false
            highlightActiveField()
            currentAmount = if (maxGoalAmount > 0) {
                String.format(Locale.getDefault(), "%.2f", maxGoalAmount)
            } else {
                "0.00"
            }
            updateAmountDisplay()
        }

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

        // Set default amount display
        updateAmountDisplay()
    }



    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Save button
        btnSaveBudget.setOnClickListener {
            saveBudgetGoal()
        }

        // Cancel button
        btnCancel.setOnClickListener {
            finish()
        }

        // Set up numeric keypad
        setupNumericKeypad()
    }

    private fun setupNumericKeypad() {
        for (i in 0..9) {
            numButtons[i].setOnClickListener { addDigit(i.toString()) }
        }

        btnDot.setOnClickListener { addDecimalPoint() }
        btnDelete.setOnClickListener { deleteLastDigit() }
    }

    private fun addDigit(digit: String) {
        if (currentAmount == "0.00") currentAmount = ""
        val newAmount = currentAmount + digit
        if (isValidCurrencyFormat(newAmount)) {
            currentAmount = newAmount
            updateAmountDisplay()
            updateCurrentFieldValue()
        }
    }

    private fun addDecimalPoint() {
        if (!currentAmount.contains(".")) {
            currentAmount += "."
            updateAmountDisplay()
            updateCurrentFieldValue()
        }
    }

    private fun deleteLastDigit() {
        if (currentAmount.isNotEmpty()) {
            currentAmount = currentAmount.dropLast(1)
            if (currentAmount.isEmpty() || currentAmount == "0.0") currentAmount = "0.00"
            updateAmountDisplay()
            updateCurrentFieldValue()
        }
    }

    private fun isValidCurrencyFormat(input: String): Boolean {
        return input.matches(Regex("\\d{0,7}(\\.\\d{0,2})?"))
    }

    private fun updateAmountDisplay() {
        tvKeypadAmount.text = currentAmount
    }

    private fun updateCurrentFieldValue() {
        try {
            val amount = currentAmount.toDouble()
            if (editingMinimum) {
                minGoalAmount = amount
                tvMinimumBudget.text = formatNumber(amount)
            } else {
                maxGoalAmount = amount
                tvMaximumBudget.text = formatNumber(amount)
            }
        } catch (e: NumberFormatException) {
            // Handle parsing error
        }
    }

    // Helper method to format numbers with commas for thousands
    private fun formatNumber(number: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(number)
    }


    private fun highlightActiveField() {
        // Get the container views
        val minimumBudgetContainer = findViewById<LinearLayout>(R.id.minimumBudgetContainer)
        val maximumBudgetContainer = findViewById<LinearLayout>(R.id.maximumBudgetContainer)

        // Visual indication of which field is being edited
        if (editingMinimum) {
            minimumBudgetContainer.setBackgroundResource(R.drawable.budget_field_active_wireframe)
            maximumBudgetContainer.setBackgroundResource(R.drawable.budget_field_rounded)
        } else {
            minimumBudgetContainer.setBackgroundResource(R.drawable.budget_field_rounded)
            maximumBudgetContainer.setBackgroundResource(R.drawable.budget_field_active_wireframe)
        }
    }


    private fun loadCurrentBudgetGoal() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.getCurrentBudgetGoal().observe(this) { budgetGoal ->
            budgetGoal?.let {
                // Set values from existing budget goal
                minGoalAmount = it.minGoalAmount
                maxGoalAmount = it.maxGoalAmount

                // Update UI
                tvMinimumBudget.text = formatNumber(minGoalAmount)
                tvMaximumBudget.text = formatNumber(maxGoalAmount)

                // Set initial editing value
                currentAmount = if (editingMinimum) {
                    String.format(Locale.getDefault(), "%.2f", minGoalAmount)
                } else {
                    String.format(Locale.getDefault(), "%.2f", maxGoalAmount)
                }

                updateAmountDisplay()
            }
        }
    }

    private fun saveBudgetGoal() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate inputs
        if (minGoalAmount <= 0) {
            Toast.makeText(this, "Please set a minimum budget goal", Toast.LENGTH_SHORT).show()
            return
        }

        if (maxGoalAmount <= 0) {
            Toast.makeText(this, "Please set a maximum budget goal", Toast.LENGTH_SHORT).show()
            return
        }

        if (minGoalAmount > maxGoalAmount) {
            Toast.makeText(this, "Minimum budget cannot be greater than maximum budget", Toast.LENGTH_SHORT).show()
            return
        }

        // Create and save budget goal
        viewModel.createBudgetGoal(
            minAmount = minGoalAmount,
            maxAmount = maxGoalAmount,
            startDate = startDate.time,
            endDate = endDate.time
        )

        Toast.makeText(this, "Budget goals saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}