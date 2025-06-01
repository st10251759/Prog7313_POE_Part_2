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
 Title: How to Make a Simple NumPad in Android Studio | Kotlin and Java
 Author: study with nesya
 Date Published: 28 March 2022
 Date Accessed: 20 May 2025
 Code Version: N/A
 Availability: https://www.youtube.com/watch?v=OFhPRXuxqgY

  --------------------------------Code Attribution----------------------------------
*/


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

class BudgetGoalsActivity : BaseActivity() {

    // ViewModel and Firebase authentication instances
    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components for budget input and controls
    private lateinit var btnBack: ImageButton
    private lateinit var tvMinimumBudget: TextView
    private lateinit var tvMaximumBudget: TextView
    private lateinit var tvKeypadAmount: TextView
    private lateinit var minimumBudgetContainer: LinearLayout
    private lateinit var maximumBudgetContainer: LinearLayout
    private lateinit var btnSaveBudget: Button
    private lateinit var btnCancel: Button
    private lateinit var btnThemeToggle: ImageButton

    // Numeric keypad buttons (0–9, decimal, delete)
    private lateinit var numButtons: List<Button>
    private lateinit var btnDot: Button
    private lateinit var btnDelete: Button

    // State variables to track user input and which field is being edited
    private var currentAmount = "0.00"    // The string representing the value being entered on keypad
    private var isEditingMinimum = true   // True if editing the minimum budget, false if editing maximum
    private var minimumAmount = 0.0       // Current minimum budget amount
    private var maximumAmount = 0.0       // Current maximum budget amount

    // Formatter to display currency values with two decimals and thousand separators
    private val decimalFormat = DecimalFormat("#,##0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()                     // Allow UI to render under system bars
        setContentView(R.layout.activity_budget_goals) // Set layout for this activity

        // Adjust padding to avoid system bars (status/navigation) overlapping content
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth for retrieving current user ID
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel for data operations (e.g., saving/loading budget goal)
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize references to UI components from layout
        initializeUI()
        // Set up click and input listeners for buttons and keypad
        setupListeners()
        // Load the currently saved budget goal (if any) to populate fields
        loadCurrentBudgetGoal()
    }

    private fun initializeUI() {
        // Bind UI views to variables
        btnBack = findViewById(R.id.btnBack)
        tvMinimumBudget = findViewById(R.id.tvMinimumBudget)
        tvMaximumBudget = findViewById(R.id.tvMaximumBudget)
        tvKeypadAmount = findViewById(R.id.tvKeypadAmount)
        minimumBudgetContainer = findViewById(R.id.minimumBudgetContainer)
        maximumBudgetContainer = findViewById(R.id.maximumBudgetContainer)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        btnCancel = findViewById(R.id.btnCancel)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)

        // Initialize numeric keypad number buttons (0 through 9)
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
        // Initialize decimal point and delete buttons
        btnDot = findViewById(R.id.btnDot)
        btnDelete = findViewById(R.id.btnDelete)

        // Set up theme toggle button using BaseActivity method
        setupThemeToggle(btnThemeToggle)

        // Initially highlight the minimum budget field as selected
        updateSelectionHighlight()
        // Update displayed amounts to their current values (initially 0.00)
        updateAmountDisplays()
    }

    private fun setupListeners() {
        // Back button closes the activity
        btnBack.setOnClickListener {
            finish()
        }

        // When minimum budget container is clicked, switch to editing minimum
        minimumBudgetContainer.setOnClickListener {
            isEditingMinimum = true               // Now editing minimum budget
            updateSelectionHighlight()             // Visually highlight minimum field
            currentAmount = if (minimumAmount > 0) minimumAmount.toString() else "0.00"
            updateKeypadDisplay()                  // Show current minimum amount on keypad
        }

        // When maximum budget container is clicked, switch to editing maximum
        maximumBudgetContainer.setOnClickListener {
            isEditingMinimum = false              // Now editing maximum budget
            updateSelectionHighlight()            // Visually highlight maximum field
            currentAmount = if (maximumAmount > 0) maximumAmount.toString() else "0.00"
            updateKeypadDisplay()                 // Show current maximum amount on keypad
        }

        // Save button click triggers validation and saving budget goal
        btnSaveBudget.setOnClickListener {
            saveBudgetGoal()
        }

        // Cancel button closes the activity without saving
        btnCancel.setOnClickListener {
            finish()
        }

        // Attach listeners to numeric keypad buttons for digit and decimal input
        setupNumericKeypad()
    }

    private fun setupNumericKeypad() {
        // Attach click listeners to number buttons 0–9
        for (i in 0..9) {
            numButtons[i].setOnClickListener { addDigit(i.toString()) }
        }
        // Attach click listener for decimal point button
        btnDot.setOnClickListener { addDecimalPoint() }
        // Attach click listener for delete (backspace) button
        btnDelete.setOnClickListener { deleteLastDigit() }
    }

    private fun addDigit(digit: String) {
        // If the display was "0.00", start fresh
        if (currentAmount == "0.00") currentAmount = ""
        // Append the new digit
        val newAmount = currentAmount + digit
        // Only update if the new string matches a valid currency format
        if (isValidCurrencyFormat(newAmount)) {
            currentAmount = newAmount
            updateCurrentBudgetValue()  // Parse and update the numeric state (min or max)
            updateKeypadDisplay()       // Update keypad display text
            updateAmountDisplays()      // Update the main budget fields
        }
    }

    private fun addDecimalPoint() {
        // Only allow one decimal point in the string
        if (!currentAmount.contains(".")) {
            currentAmount += "."
            updateKeypadDisplay()  // Refresh the keypad display
        }
    }

    private fun deleteLastDigit() {
        // Remove the last character if any
        if (currentAmount.isNotEmpty()) {
            currentAmount = currentAmount.dropLast(1)
            // If all digits deleted, reset to "0.00"
            if (currentAmount.isEmpty()) currentAmount = "0.00"
            updateCurrentBudgetValue()  // Update numeric state after deletion
            updateKeypadDisplay()       // Refresh keypad display
            updateAmountDisplays()      // Refresh main budget fields
        }
    }

    private fun isValidCurrencyFormat(input: String): Boolean {
        // Ensure at most 7 digits before decimal and up to 2 digits after
        return input.matches(Regex("\\d{0,7}(\\.\\d{0,2})?"))
    }

    private fun updateCurrentBudgetValue() {
        try {
            // Parse currentAmount string to double
            val amount = currentAmount.toDouble()
            // Assign parsed value to either minimum or maximum based on selection
            if (isEditingMinimum) {
                minimumAmount = amount
            } else {
                maximumAmount = amount
            }
        } catch (e: NumberFormatException) {
            // If parsing fails, do nothing (invalid input is prevented by format check)
        }
    }

    private fun updateSelectionHighlight() {
        // Highlight the selected budget container and dim the other
        if (isEditingMinimum) {
            minimumBudgetContainer.setBackgroundResource(R.drawable.budget_field_selected)
            maximumBudgetContainer.setBackgroundResource(R.drawable.budget_field_rounded)
        } else {
            minimumBudgetContainer.setBackgroundResource(R.drawable.budget_field_rounded)
            maximumBudgetContainer.setBackgroundResource(R.drawable.budget_field_selected)
        }
    }

    private fun updateKeypadDisplay() {
        // Show the currentAmount string in the keypad display field
        tvKeypadAmount.text = currentAmount
    }

    private fun updateAmountDisplays() {
        // Format and display the numeric values of minimum and maximum amounts
        tvMinimumBudget.text = decimalFormat.format(minimumAmount)
        tvMaximumBudget.text = decimalFormat.format(maximumAmount)
    }

    private fun loadCurrentBudgetGoal() {
        // Get current user ID from Firebase Auth; return if no user
        val userId = auth.currentUser?.uid ?: return

        // Observe the budget goal LiveData from ViewModel and update UI when it arrives
        viewModel.getCurrentBudgetGoal(userId).observe(this) { budgetGoal ->
            budgetGoal?.let {
                // Populate minimum and maximum with saved goal values
                minimumAmount = it.minGoalAmount
                maximumAmount = it.maxGoalAmount
                updateAmountDisplays()  // Refresh displayed amounts

                // Update currentAmount to reflect whichever field is being edited
                currentAmount = if (isEditingMinimum) {
                    if (minimumAmount > 0) minimumAmount.toString() else "0.00"
                } else {
                    if (maximumAmount > 0) maximumAmount.toString() else "0.00"
                }
                updateKeypadDisplay() // Refresh the keypad display with loaded value
            }
        }
    }

    private fun saveBudgetGoal() {
        // Validate minimum amount must be greater than zero
        if (minimumAmount <= 0) {
            Toast.makeText(this, "Please set a minimum budget amount", Toast.LENGTH_SHORT).show()
            return
        }
        // Validate maximum amount must be greater than zero
        if (maximumAmount <= 0) {
            Toast.makeText(this, "Please set a maximum budget amount", Toast.LENGTH_SHORT).show()
            return
        }
        // Ensure minimum is less than maximum
        if (minimumAmount >= maximumAmount) {
            Toast.makeText(this, "Maximum budget must be greater than minimum budget", Toast.LENGTH_SHORT).show()
            return
        }

        // Create start of current month at 00:00:00.000 for the budget period
        val startDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Create end of current month at 23:59:59.999 for the budget period
        val endDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        // Use ViewModel to persist the new budget goal to Firestore (or local DB)
        viewModel.createBudgetGoal(minimumAmount, maximumAmount, startDate, endDate)

        // Notify user of success and close the activity
        Toast.makeText(this, "Budget goal saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
