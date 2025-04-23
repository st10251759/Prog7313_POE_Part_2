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

// Import necessary Android and Kotlin libraries
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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

    // Firebase ViewModel and authentication
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

    // Budget goal values and dates
    private var currentAmount = "0.00"
    private var minGoalAmount = 0.00
    private var maxGoalAmount = 0.00

    // Set default start and end date for budget goal to current month
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

    // Formatter for displaying month and year
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

        // Begin with editing the minimum budget
        editingMinimum = true
        highlightActiveField()

        // Load existing budget goal if any
        loadCurrentBudgetGoal()
    }

    // Find and assign views from layout
    private fun initializeUI() {
        btnBack = findViewById(R.id.btnBack)
        tvMinimumBudget = findViewById(R.id.tvMinimumBudget)
        tvMaximumBudget = findViewById(R.id.tvMaximumBudget)
        tvKeypadAmount = findViewById(R.id.tvKeypadAmount)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        btnCancel = findViewById(R.id.btnCancel)

        // Allow switching between editing min and max values
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

        // Initialize number keypad buttons
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


    // Set button click listeners
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

    // Set up numeric keypad
    private fun setupNumericKeypad() {
        for (i in 0..9) {
            numButtons[i].setOnClickListener { addDigit(i.toString()) }
        }

        btnDot.setOnClickListener { addDecimalPoint() }
        btnDelete.setOnClickListener { deleteLastDigit() }
    }

    // Adds a digit to the current input
    private fun addDigit(digit: String) {
        if (currentAmount == "0.00") currentAmount = ""
        val newAmount = currentAmount + digit
        if (isValidCurrencyFormat(newAmount)) {
            currentAmount = newAmount
            updateAmountDisplay()
            updateCurrentFieldValue()
        }
    }

    // Adds a decimal point if not already present
    private fun addDecimalPoint() {
        if (!currentAmount.contains(".")) {
            currentAmount += "."
            updateAmountDisplay()
            updateCurrentFieldValue()
        }
    }

    // Deletes the last digit entered
    private fun deleteLastDigit() {
        if (currentAmount.isNotEmpty()) {
            currentAmount = currentAmount.dropLast(1)
            if (currentAmount.isEmpty() || currentAmount == "0.0") currentAmount = "0.00"
            updateAmountDisplay()
            updateCurrentFieldValue()
        }
    }

    // Ensures input has a valid currency format (up to 2 decimal places)
    private fun isValidCurrencyFormat(input: String): Boolean {
        return input.matches(Regex("\\d{0,7}(\\.\\d{0,2})?"))
    }

    // Updates amount shown in TextView
    private fun updateAmountDisplay() {
        tvKeypadAmount.text = currentAmount
    }

    /*
    --------------------------------Code Attribution----------------------------------
    Title: Exceptions
    Author: Kotlin
    Date Published: 2025
    Date Accessed: 16 April 2025
    Code Version: v.2.1.20
    Availability: https://kotlinlang.org/docs/exceptions.html#handle-exceptions-using-try-catch-blocks
    --------------------------------Code Attribution----------------------------------
    */

    // Updates the selected budget field with the new value
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
            //Handle Parsing if incorrect number format
            Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show()
            Log.e("BudgetInput", "Failed to parse amount: $currentAmount", e)
        }
    }

    // Format the number as currency with commas and 2 decimal places
    private fun formatNumber(number: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(number)
    }

    // Visually indicate which budget value is being edited
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

    // Loads the current user's budget goals (if any) from the database
    private fun loadCurrentBudgetGoal() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Reflect values on UI
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

                //Display refreshed amounts
                updateAmountDisplay()
            }
        }
    }

    // Validates and saves the budget goal to the database
    private fun saveBudgetGoal() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate inputs so they are not null
        if (minGoalAmount <= 0) {
            Toast.makeText(this, "Please set a minimum budget goal", Toast.LENGTH_SHORT).show()
            return
        }

        if (maxGoalAmount <= 0) {
            Toast.makeText(this, "Please set a maximum budget goal", Toast.LENGTH_SHORT).show()
            return
        }

        //Validate inputs so that Min budget is not greater than max budget
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

        //Display success message
        Toast.makeText(this, "Budget goals saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}