package com.firstproject.prog7313_budgetbuddy

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat
import java.util.*

class BudgetGoalsActivity : AppCompatActivity() {

    // ViewModel and Firebase authentication
    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var tvMinimumBudget: TextView
    private lateinit var tvMaximumBudget: TextView
    private lateinit var tvKeypadAmount: TextView
    private lateinit var minimumBudgetContainer: LinearLayout
    private lateinit var maximumBudgetContainer: LinearLayout
    private lateinit var btnSaveBudget: Button
    private lateinit var btnCancel: Button

    // Numeric keypad buttons
    private lateinit var numButtons: List<Button>
    private lateinit var btnDot: Button
    private lateinit var btnDelete: Button

    // State variables
    private var currentAmount = "0.00"
    private var isEditingMinimum = true
    private var minimumAmount = 0.0
    private var maximumAmount = 0.0

    // Decimal formatter
    private val decimalFormat = DecimalFormat("#,##0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget_goals)

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
        loadCurrentBudgetGoal()
    }

    private fun initializeUI() {
        btnBack = findViewById(R.id.btnBack)
        tvMinimumBudget = findViewById(R.id.tvMinimumBudget)
        tvMaximumBudget = findViewById(R.id.tvMaximumBudget)
        tvKeypadAmount = findViewById(R.id.tvKeypadAmount)
        minimumBudgetContainer = findViewById(R.id.minimumBudgetContainer)
        maximumBudgetContainer = findViewById(R.id.maximumBudgetContainer)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        btnCancel = findViewById(R.id.btnCancel)

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

        // Set initial selection to minimum budget
        updateSelectionHighlight()
        updateAmountDisplays()
    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Budget container click listeners
        minimumBudgetContainer.setOnClickListener {
            isEditingMinimum = true
            updateSelectionHighlight()
            currentAmount = if (minimumAmount > 0) minimumAmount.toString() else "0.00"
            updateKeypadDisplay()
        }

        maximumBudgetContainer.setOnClickListener {
            isEditingMinimum = false
            updateSelectionHighlight()
            currentAmount = if (maximumAmount > 0) maximumAmount.toString() else "0.00"
            updateKeypadDisplay()
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
        // Loop through the digits 0 to 9
        for (i in 0..9) {
            numButtons[i].setOnClickListener { addDigit(i.toString()) }
        }

        // Decimal point button
        btnDot.setOnClickListener { addDecimalPoint() }

        // Delete button
        btnDelete.setOnClickListener { deleteLastDigit() }
    }

    private fun addDigit(digit: String) {
        if (currentAmount == "0.00") currentAmount = ""
        val newAmount = currentAmount + digit
        if (isValidCurrencyFormat(newAmount)) {
            currentAmount = newAmount
            updateCurrentBudgetValue()
            updateKeypadDisplay()
            updateAmountDisplays()
        }
    }

    private fun addDecimalPoint() {
        if (!currentAmount.contains(".")) {
            currentAmount += "."
            updateKeypadDisplay()
        }
    }

    private fun deleteLastDigit() {
        if (currentAmount.isNotEmpty()) {
            currentAmount = currentAmount.dropLast(1)
            if (currentAmount.isEmpty()) currentAmount = "0.00"
            updateCurrentBudgetValue()
            updateKeypadDisplay()
            updateAmountDisplays()
        }
    }

    private fun isValidCurrencyFormat(input: String): Boolean {
        return input.matches(Regex("\\d{0,7}(\\.\\d{0,2})?"))
    }

    private fun updateCurrentBudgetValue() {
        try {
            val amount = currentAmount.toDouble()
            if (isEditingMinimum) {
                minimumAmount = amount
            } else {
                maximumAmount = amount
            }
        } catch (e: NumberFormatException) {
            // Handle error
        }
    }

    private fun updateSelectionHighlight() {
        // Update background colors to show which budget is being edited
        if (isEditingMinimum) {
            minimumBudgetContainer.setBackgroundResource(R.drawable.budget_field_selected)
            maximumBudgetContainer.setBackgroundResource(R.drawable.budget_field_rounded)
        } else {
            minimumBudgetContainer.setBackgroundResource(R.drawable.budget_field_rounded)
            maximumBudgetContainer.setBackgroundResource(R.drawable.budget_field_selected)
        }
    }

    private fun updateKeypadDisplay() {
        tvKeypadAmount.text = currentAmount
    }

    private fun updateAmountDisplays() {
        tvMinimumBudget.text = decimalFormat.format(minimumAmount)
        tvMaximumBudget.text = decimalFormat.format(maximumAmount)
    }

    private fun loadCurrentBudgetGoal() {
        val userId = auth.currentUser?.uid ?: return

        viewModel.getCurrentBudgetGoal(userId).observe(this) { budgetGoal ->
            budgetGoal?.let {
                minimumAmount = it.minGoalAmount
                maximumAmount = it.maxGoalAmount
                updateAmountDisplays()

                // Update current amount to show the currently selected budget
                currentAmount = if (isEditingMinimum) {
                    if (minimumAmount > 0) minimumAmount.toString() else "0.00"
                } else {
                    if (maximumAmount > 0) maximumAmount.toString() else "0.00"
                }
                updateKeypadDisplay()
            }
        }
    }

    private fun saveBudgetGoal() {
        // Validation
        if (minimumAmount <= 0) {
            Toast.makeText(this, "Please set a minimum budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (maximumAmount <= 0) {
            Toast.makeText(this, "Please set a maximum budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (minimumAmount >= maximumAmount) {
            Toast.makeText(this, "Maximum budget must be greater than minimum budget", Toast.LENGTH_SHORT).show()
            return
        }

        // Create date range for current month
        val startDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        // Save budget goal using ViewModel
        viewModel.createBudgetGoal(minimumAmount, maximumAmount, startDate, endDate)

        Toast.makeText(this, "Budget goal saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}