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
 Title: MPAndriodChart
 Author: Philipp Jahoda
 Date Published: 20 March 2019
 Date Accessed: 26 May 2025
 Code Version: 3.1.0
 Availability: https://github.com/PhilJay/MPAndroidChart/tree/master

  --------------------------------Code Attribution----------------------------------
*/

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.firstproject.prog7313_budgetbuddy.data.models.*
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : BaseActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var btnThemeToggle: ImageButton
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvDailyAverage: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var tvBudgetGoal: TextView
    private lateinit var tvChartTitle: TextView
    private lateinit var tvSelectedDateRange: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var noDataLayout: LinearLayout
    private lateinit var chartLegend: LinearLayout

    // Date filter components
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var customDateLayout: LinearLayout

    // Period selection buttons
    private lateinit var btnWeekPeriod: TextView
    private lateinit var btnMonthPeriod: TextView
    private lateinit var btnYearPeriod: TextView
    private lateinit var btnCustomPeriod: TextView

    // Chart type buttons
    private lateinit var btnLineChart: TextView
    private lateinit var btnPieChart: TextView
    private lateinit var btnBarChart: TextView

    // Charts
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    // Current selections
    private var currentPeriod = TimePeriod.WEEK
    private var currentChartType = ChartType.LINE
    private var isCustomDateRange = false

    // Custom date range
    private var customStartDate: Calendar? = null
    private var customEndDate: Calendar? = null

    // Data
    private var currentExpenses: List<Expense> = emptyList()
    private var currentBudgetGoal: BudgetGoal? = null

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    companion object {
        private const val TAG = "AnalyticsActivity"
    }

    // Override the onCreate lifecycle method of the Activity.
// This method is called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call the superclass implementation to ensure proper activity lifecycle handling.
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout support so that the app can draw behind system bars (status bar and navigation bar).
        enableEdgeToEdge()

        // Set the layout file to be used for this activity's UI.
        // This inflates the XML layout resource 'activity_analytics' and makes it the visible view.
        setContentView(R.layout.activity_analytics)

        // Apply window insets listener to handle layout padding for system bars (e.g., notch, navigation bar).
        // This ensures that the layout avoids overlapping system UI areas.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Retrieve the insets for system bars (top, bottom, left, right areas where system UI appears).
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply the appropriate padding to the main view to avoid UI overlap.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            // Return the insets object to indicate the insets have been consumed.
            insets
        }

        // Initialize Firebase Authentication instance.
        // This will be used to check if a user is logged in.
        auth = FirebaseAuth.getInstance()

        // Check if there is no authenticated user currently logged in.
        if (auth.currentUser == null) {
            // If not, end the activity and return immediately.
            // This prevents unauthorized access to the analytics screen.
            finish()
            return
        }

        // Create and retrieve an instance of the ViewModel for this activity.
        // ViewModelProvider ensures that the ViewModel is lifecycle-aware and survives configuration changes.
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize the UI components and populate static or default UI state.
        initializeUI()

        // Set up event listeners for user interactions (e.g., button clicks, input changes).
        setupListeners()

        // Load necessary data (e.g., from Firebase or local cache) and bind it to the UI.
        loadData()
    }

    private fun initializeUI() {
        // Assign the back button view to the btnBack variable so it can be handled programmatically (e.g., onClick).
        btnBack = findViewById(R.id.btnBack)
        // Assign the theme toggle button view to enable light/dark mode switching.
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
        // TextView showing the user's total expenses for the selected period.
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        // TextView displaying the average amount spent per day during the selected date range.
        tvDailyAverage = findViewById(R.id.tvDailyAverage)
        // TextView showing the current budget status, e.g., "Within Budget" or "Over Budget."
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        // TextView displaying the user's budget goal or limit.
        tvBudgetGoal = findViewById(R.id.tvBudgetGoal)
        // Title for the chart section, such as "Spending Trends" or "Category Breakdown."
        tvChartTitle = findViewById(R.id.tvChartTitle)
        // TextView that displays the currently selected date range (e.g., "Jan 1 – Jan 31").
        tvSelectedDateRange = findViewById(R.id.tvSelectedDateRange)
        // Progress bar that appears during data loading (e.g., fetching from Firebase).
        progressBar = findViewById(R.id.progressBar)
        // Layout displayed when no expense data is available (i.e., empty state UI).
        noDataLayout = findViewById(R.id.noDataLayout)
        // UI component showing a legend for the chart, e.g., category colors for a pie chart.
        chartLegend = findViewById(R.id.chartLegend)

        // ---------------- Date filter inputs ----------------
        // EditText to let the user pick a custom "from" date for filtering.
        etFromDate = findViewById(R.id.etFromDate)
        // EditText to let the user pick a custom "to" date for filtering.
        etToDate = findViewById(R.id.etToDate)
        // Layout that contains the custom date range UI components.
        customDateLayout = findViewById(R.id.customDateLayout)

        // ---------------- Period filter buttons ----------------
        // Button to quickly filter data for the current week.
        btnWeekPeriod = findViewById(R.id.btnWeekPeriod)
        // Button to filter data for the current month.
        btnMonthPeriod = findViewById(R.id.btnMonthPeriod)
        // Button to filter data for the current year.
        btnYearPeriod = findViewById(R.id.btnYearPeriod)
        // Button to enable custom date range selection.
        btnCustomPeriod = findViewById(R.id.btnCustomPeriod)

        // ---------------- Chart type buttons ----------------
        // Button to select and display a line chart of expenses.
        btnLineChart = findViewById(R.id.btnLineChart)
        // Button to select and display a pie chart of expenses.
        btnPieChart = findViewById(R.id.btnPieChart)
        // Button to select and display a bar chart of expenses.
        btnBarChart = findViewById(R.id.btnBarChart)

        // ---------------- Chart Views ----------------
        // Line chart view for visualizing expense trends over time.
        lineChart = findViewById(R.id.lineChart)
        // Pie chart view for displaying category-based breakdowns of expenses.
        pieChart = findViewById(R.id.pieChart)
        // Bar chart view for showing comparative values such as weekly or monthly spending.
        barChart = findViewById(R.id.barChart)

        // Set up initial chart configurations (e.g., styling, axis, legend settings).
        setupCharts()

        // Automatically update and display the default or current date range selection.
        updateSelectedDateRange()

        // Initialize the theme toggle feature and apply current theme state (e.g., light or dark mode).
        setupThemeToggle(btnThemeToggle)
    }


    private fun setupCharts() {
        // Setup Line Chart with better styling
        lineChart.apply {
            description.isEnabled = false  // Disable the chart's built-in description label
            setTouchEnabled(true)  // Allow users to touch and interact with the chart
            isDragEnabled = true  // Enable dragging (scrolling) across the chart
            setScaleEnabled(true)  // Enable zooming on the chart (both axes)
            setPinchZoom(true)  // Enable pinch-to-zoom that affects both axes proportionally
            setDrawGridBackground(false)  // Disable the shaded grid background inside the chart area
            legend.isEnabled = false  // Hide the legend (labels explaining data series)

            // Styling
            setBackgroundColor(Color.TRANSPARENT) // Set transparent chart background for theme consistency
            setBorderColor(
                ContextCompat.getColor(
                    this@AnalyticsActivity,
                    R.color.asparagus
                )
            ) // Apply a custom border color using theme color (e.g., green)
            setBorderWidth(1f)  // Set border thickness

            xAxis.apply {
                position =
                    XAxis.XAxisPosition.BOTTOM   // Move the X-axis labels to the bottom of the chart
                setDrawGridLines(true)  // Enable vertical grid lines
                gridColor = Color.LTGRAY  // Use light gray grid line color
                gridLineWidth = 0.5f  // Thin grid lines
                granularity = 1f   // Display a label every 1 unit
                textColor = ContextCompat.getColor(
                    this@AnalyticsActivity,
                    R.color.text_secondary
                )  // Set axis label color
                textSize = 10f   // Set axis label font size
                setAvoidFirstLastClipping(true)  // Prevent the first and last X-axis labels from being cut off
            }

            axisRight.isEnabled = false   // Disable right Y-axis for simplicity
            axisLeft.apply {
                setDrawGridLines(true)  // Enable horizontal grid lines
                gridColor = Color.LTGRAY
                gridLineWidth = 0.5f
                setDrawZeroLine(true)  // Draw a horizontal line at value 0
                zeroLineColor = ContextCompat.getColor(
                    this@AnalyticsActivity,
                    R.color.text_tertiary
                )  // Customize Y-axis label style
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
                setDrawAxisLine(false)  // Hide the vertical Y-axis line
                axisMinimum = 0f    // Ensure Y-axis starts at 0

                // Format Y-axis values as currency with proper intervals
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000 -> "R${(value / 1000).toInt()}k"  // Convert large numbers to "R1k"
                            else -> "R${value.toInt()}"    // Normal formatting
                        }
                    }
                }
            }

            // Enable tap and drag to highlight specific data points
            isHighlightPerDragEnabled = true
            isHighlightPerTapEnabled = true
        }

        // Setup Pie Chart with better styling
        pieChart.apply {
            description.isEnabled = false  //// Disable the default description label
            setUsePercentValues(true)  // Interpret data as percentages (useful for visualizing proportions)
            setEntryLabelColor(
                ContextCompat.getColor(
                    this@AnalyticsActivity,
                    R.color.text_primary
                )
            )  // Set the color of entry labels (text on pie slices)
            setEntryLabelTextSize(11f)  // Set size of entry label text
            centerText =
                "Spending\nBreakdown"   // Set the center text within the pie chart (multi-line)
            setCenterTextSize(14f)   // Set size and color for center text
            setCenterTextColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.text_primary))
            holeRadius = 45f  // Customize the inner radius (doughnut effect)
            transparentCircleRadius = 50f
            setHoleColor(Color.TRANSPARENT)  // Transparent hole for aesthetics
            legend.apply {  //legend key styling
                isEnabled = true
                orientation =
                    com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                verticalAlignment =
                    com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment =
                    com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
            }

            // Animation
            animateY(1000)
        }

        // Setup Bar Chart with better styling
        barChart.apply {
            description.isEnabled = false   // Disable chart description label
            setTouchEnabled(true)  // Allow interaction
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)  // Disable pinch-zoom for clarity (bar charts often use simple zooming)
            setDrawGridBackground(false)
            legend.apply {
                isEnabled = true
                orientation =
                    com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                verticalAlignment =
                    com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment =
                    com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
            }
            setFitBars(true)  // Ensure bars are aligned properly with axis ticks

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
                labelRotationAngle = 0f
                setAvoidFirstLastClipping(true)
                // Don't show labels on X-axis for bar chart
                setDrawLabels(false)  // Hide labels (if using custom icons or space-saving)
            }

            axisRight.isEnabled = false    // Hide right Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                gridLineWidth = 0.5f
                setDrawZeroLine(false)
                axisMinimum = 0f
                textColor = ContextCompat.getColor(
                    this@AnalyticsActivity,
                    R.color.text_secondary
                )  // Style for Y-axis text
                textSize = 10f

                // Format Y-axis values as currency
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000 -> "R${(value / 1000).toInt()}k"
                            else -> "R${value.toInt()}"
                        }
                    }
                }
            }

            // Animate bar heights growing from bottom
            animateY(1000)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }


        // Period selection
        btnWeekPeriod.setOnClickListener { selectPeriod(TimePeriod.WEEK) }
        btnMonthPeriod.setOnClickListener { selectPeriod(TimePeriod.MONTH) }
        btnYearPeriod.setOnClickListener { selectPeriod(TimePeriod.YEAR) }
        btnCustomPeriod.setOnClickListener { selectCustomPeriod() }

        // Chart type selection
        btnLineChart.setOnClickListener { selectChartType(ChartType.LINE) }
        btnPieChart.setOnClickListener { selectChartType(ChartType.PIE) }
        btnBarChart.setOnClickListener { selectChartType(ChartType.BAR) }

        // Custom date selection
        etFromDate.setOnClickListener { showDatePicker(true) }
        etToDate.setOnClickListener { showDatePicker(false) }
    }

    /**
     * Selects a predefined time period (e.g., Daily, Weekly, Monthly).
     * This function updates the state of the UI and data accordingly.
     */
    private fun selectPeriod(period: TimePeriod) {
        // Store the selected time period in a class-level variable to keep track of the current view state
        currentPeriod = period
        // Since a predefined time period was selected (not a custom range), set the custom range flag to false
        isCustomDateRange = false
        // Hide the custom date range layout from the user interface to avoid confusion
        customDateLayout.visibility = View.GONE
        // Visually update the buttons so the selected period button appears highlighted/active
        updatePeriodButtons()
        // Update the displayed date range label or information according to the newly selected period
        updateSelectedDateRange()
        // Reload the data from your source (e.g., Firebase, SQLite, etc.) based on the new time period
        loadData()
    }

    /**
     * Called when the user selects the option to use a custom date range.
     * This method configures the app to display and work with a user-defined date period
     * rather than a predefined time frame like "This Week" or "This Month".
     */
    private fun selectCustomPeriod() {
        // Set the flag to indicate that a custom date range is now in use
        isCustomDateRange = true

        // Make the custom date range input fields visible (e.g., "From Date" and "To Date" text fields)
        customDateLayout.visibility = View.VISIBLE

        // Refresh the appearance of all period buttons (e.g., unselect week/month/year and highlight "custom")
        updatePeriodButtons()

        // Check if the start date for the custom period has already been set
        if (customStartDate == null) {
            // If not set, initialize it to 30 days ago from today as a sensible default range
            customStartDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30) // Subtract 30 days
            }
        }

        // Check if the end date for the custom period has already been set
        if (customEndDate == null) {
            // If not set, use the current date as the default end date
            customEndDate = Calendar.getInstance()
        }

        // Update the UI fields to display the selected or default custom start and end dates
        updateCustomDateFields()

        // Refresh the visible text showing the selected date range (e.g., "1 May - 31 May")
        updateSelectedDateRange()

        // Load and display data based on the custom date range selected by the user
        loadData()
    }


    /**
     * Handles user interaction when selecting a different type of chart (e.g., Line, Bar, or Pie).
     * This method updates the current chart state, adjusts the UI buttons to reflect the selection,
     * and refreshes the chart view to display the selected chart type with the appropriate data.
     */
    private fun selectChartType(chartType: ChartType) {
        // Update the state variable to store the newly selected chart type.
        // This will be used later to determine which chart (line, pie, or bar) should be displayed.
        currentChartType = chartType

        // Update the appearance of the chart type toggle buttons (e.g., highlight the selected one).
        // This provides visual feedback to the user indicating which chart is currently active.
        updateChartTypeButtons()

        // Refresh the chart display by rendering the correct chart type with its corresponding data.
        // This typically involves hiding other chart views and showing only the selected one.
        updateChart()
    }


    /**
     * Displays a date picker dialog to the user, allowing selection of a custom start or end date.
     *
     * parameter isStartDate Boolean flag indicating whether the date being selected is a start date (true)
     *                    or an end date (false) in the custom date range.
     */
    private fun showDatePicker(isStartDate: Boolean) {
        // Initialize a calendar instance to be shown in the date picker by default.
        // If selecting the start date and it's already set, use it; otherwise default to 30 days ago.
        // If selecting the end date and it's already set, use it; otherwise default to today.
        val calendar = if (isStartDate) {
            customStartDate ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
        } else {
            customEndDate ?: Calendar.getInstance()
        }

        // Create and show a DatePickerDialog initialized with the date from the calendar above.
        DatePickerDialog(
            this,  // Context (typically the activity) in which the dialog should be shown
            { _, year, month, dayOfMonth ->  // Lambda to handle the user's selected date
                // Create a new Calendar instance to represent the selected date
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)  // Set the year, month, and day

                    // Set the time component based on whether it's a start or end date
                    if (isStartDate) {
                        // Set time to the very beginning of the day (00:00:00.000)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    } else {
                        // Set time to the very end of the day (23:59:59.999)
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                }

                // Store the selected date in the appropriate variable
                if (isStartDate) {
                    customStartDate = selectedCalendar
                } else {
                    customEndDate = selectedCalendar
                }

                // Update the UI field(s) showing the selected custom dates
                updateCustomDateFields()

                // Recalculate and reflect the updated selected date range text or logic
                updateSelectedDateRange()

                // Reload or filter the data based on the new date range selection
                loadData()
            },
            calendar.get(Calendar.YEAR),      // Initial year to show
            calendar.get(Calendar.MONTH),     // Initial month to show
            calendar.get(Calendar.DAY_OF_MONTH) // Initial day to show
        ).show()  // Finally, display the dialog to the user
    }


    /**
     * Updates the "From" and "To" date input fields in the UI with the currently selected
     * custom start and end dates, formatting them into a readable string.
     */
    private fun updateCustomDateFields() {
        // If a custom start date has been selected (i.e., not null),
        // format that date using `dateFormat` and display it in the "From" EditText (etFromDate).
        customStartDate?.let {
            etFromDate.setText(dateFormat.format(it.time))
        }

        // If a custom end date has been selected (i.e., not null),
        // format that date using `dateFormat` and display it in the "To" EditText (etToDate).
        customEndDate?.let {
            etToDate.setText(dateFormat.format(it.time))
        }
    }


    /**
     * Updates the text view (`tvSelectedDateRange`) to show the currently selected date range.
     * It displays either a custom range (with actual dates) or a predefined period label (like "Last 30 days").
     */
    private fun updateSelectedDateRange() {
        // Determine the display text for the selected date range
        val text = if (
        // Check if the user is in custom date mode
            isCustomDateRange &&
            // Make sure both custom start and end dates are not null
            customStartDate != null &&
            customEndDate != null
        ) {
            // If using a custom range and both dates are valid, format the range as: "StartDate - EndDate"
            // `!!` is used to assert that the dates are not null (safe due to the above check)
            "${dateFormat.format(customStartDate!!.time)} - ${dateFormat.format(customEndDate!!.time)}"
        } else {
            // If not using a custom range, show a label based on the selected predefined period
            when (currentPeriod) {
                TimePeriod.WEEK -> "Last 7 days"          // For weekly reports
                TimePeriod.MONTH -> "Last 30 days"        // For monthly reports
                TimePeriod.YEAR -> "Last 365 days"        // For yearly reports
                TimePeriod.CUSTOM -> "Custom range"       // This fallback handles any edge case where custom mode is toggled but dates are unset
            }
        }

        // Update the UI: set the formatted text into the TextView that shows the current date range
        tvSelectedDateRange.text = text
    }


    /**
     * Updates the visual states of the period selection buttons to reflect the currently active period.
     * It resets all buttons to their default unselected style, then highlights the button
     * corresponding to the current selected period (or custom date range).
     */
    private fun updatePeriodButtons() {
        // Step 1: Reset all buttons to their default unselected appearance
        // We collect all period buttons into a list for easy iteration.
        listOf(btnWeekPeriod, btnMonthPeriod, btnYearPeriod, btnCustomPeriod).forEach { button ->
            // Remove any existing background drawable (clears previous highlight)
            button.background = null

            // Set the text color back to the default "asparagus" green color,
            // indicating an unselected state.
            button.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        }

        // Step 2: Determine which button corresponds to the current active period
        // If the user has selected a custom date range, the custom period button should be highlighted.
        // Otherwise, select the button corresponding to the predefined period stored in currentPeriod.
        val selectedButton = if (isCustomDateRange) {
            btnCustomPeriod
        } else {
            // Match currentPeriod enum to the appropriate button
            when (currentPeriod) {
                TimePeriod.WEEK -> btnWeekPeriod       // Highlight weekly period button
                TimePeriod.MONTH -> btnMonthPeriod     // Highlight monthly period button
                TimePeriod.YEAR -> btnYearPeriod       // Highlight yearly period button
                TimePeriod.CUSTOM -> btnCustomPeriod   // Fallback if currentPeriod is custom
            }
        }

        // Step 3: Apply selected styling to the chosen button
        // Set the button background drawable to visually indicate it is selected.
        selectedButton.background =
            ContextCompat.getDrawable(this, R.drawable.period_toggle_selected)

        // Change the button's text color to white to improve contrast with the selected background,
        // visually reinforcing that this button is active.
        selectedButton.setTextColor(Color.WHITE)
    }


    /**
     * Updates the visual appearance of chart type selection buttons.
     * This provides immediate UI feedback to indicate which chart type is currently active.
     */
    private fun updateChartTypeButtons() {
        // Reset all chart buttons to their default unselected appearance
        // Iterate over the list of all chart type buttons
        listOf(btnLineChart, btnPieChart, btnBarChart).forEach { button ->
            // Remove any existing selection highlight from each button
            button.background = null

            // Set the button text color to the default theme color ("asparagus")
            // to indicate it is not currently selected
            button.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        }

        // Determine which button matches the currently selected chart type
        // and update its appearance to show it is active
        val selectedButton = when (currentChartType) {
            ChartType.LINE -> btnLineChart  // Highlight line chart button if selected
            ChartType.PIE -> btnPieChart    // Highlight pie chart button if selected
            ChartType.BAR -> btnBarChart    // Highlight bar chart button if selected
        }

        // Apply a background drawable that visually marks the selected button
        selectedButton.background =
            ContextCompat.getDrawable(this, R.drawable.period_toggle_selected)

        // Change the text color to white to contrast against the selected background
        selectedButton.setTextColor(Color.WHITE)
    }


    /**
     * Loads the user's expenses and budget goal for the currently selected date range.
     * Fetches this data asynchronously via the ViewModel, updates the UI accordingly,
     * and shows a loading indicator while fetching.
     */
    private fun loadData() {
        // Retrieve the currently authenticated user's ID
        val userId = auth.currentUser?.uid ?: return  // Exit early if not authenticated

        // Show a loading spinner or progress bar while data is being fetched
        showLoading(true)

        // Get the start and end dates based on selected period or custom range
        val (startDate, endDate) = getDateRange()

        // Log the data fetch range for debugging purposes
        Log.d(TAG, "Loading data from $startDate to $endDate")

        // Observe the result of the expense fetch
        // `getExpensesByPeriod()` returns LiveData<List<Expense>>; we observe it to respond to updates
        viewModel.getExpensesByPeriod(userId, startDate, endDate).observe(this) { expenses ->
            // Log the number of loaded expense records
            Log.d(TAG, "Loaded ${expenses?.size ?: 0} expenses")

            // Update the local list of currently loaded expenses (or an empty list if null)
            currentExpenses = expenses ?: emptyList()

            // Fetch the current budget goal for the user (also returns LiveData)
            viewModel.getCurrentBudgetGoal(userId).observe(this) { budgetGoal ->
                // Log the retrieved budget goal for debugging
                Log.d(TAG, "Loaded budget goal: $budgetGoal")

                // Update the current budget goal in memory
                currentBudgetGoal = budgetGoal

                // Refresh the UI to reflect the new data
                updateUI()

                // Hide the loading indicator once everything is loaded and displayed
                showLoading(false)
            }
        }
    }


    /**
     * Determines and returns the start and end dates to be used for data queries.
     * It checks whether a custom date range is selected or falls back to predefined periods (week, month, year).
     */
    private fun getDateRange(): Pair<Date, Date> {
        // Check if the user has selected a custom date range and both start/end dates are set
        return if (isCustomDateRange && customStartDate != null && customEndDate != null) {
            // If so, return the custom start and end dates directly
            Pair(customStartDate!!.time, customEndDate!!.time)
        } else {
            // Otherwise, use a predefined period based on `currentPeriod`

            // Create a Calendar instance set to the current date/time (this will be the end date)
            val calendar = Calendar.getInstance()
            val endDate = calendar.time  // Save current date as the end date

            // Subtract the number of days defined by the selected TimePeriod (WEEK = 7, etc.)
            calendar.add(Calendar.DAY_OF_YEAR, -currentPeriod.days)

            // Save the calculated start date after subtraction
            val startDate = calendar.time

            // Return the start and end date as a Pair
            Pair(startDate, endDate)
        }
    }


    /**
     * Refreshes the relevant parts of the UI based on the most recent data.
     * Typically called after new data (expenses and/or budget goal) is loaded.
     */
    private fun updateUI() {
        // Update the summary section, such as total expenses and goal comparison
        updateSummary()

        // Redraw the chart with the current data and selected chart type
        updateChart()
    }

    /**
     * Updates the summary section of the UI to reflect the total amount spent,
     * the daily average, and the current budget status for the selected date range.
     */
    private fun updateSummary() {
        // Calculate the total amount spent by summing the `totalAmount` of each expense in the current list
        val totalSpent = currentExpenses.sumOf { it.totalAmount }

        // Retrieve the start and end dates of the currently selected period (custom or predefined)
        val (startDate, endDate) = getDateRange()

        // Calculate the number of days between the start and end date
        // Convert milliseconds to days by dividing by (1000 ms * 60 sec * 60 min * 24 hrs)
        // Add 1 to include both start and end dates in the range
        val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1

        // Calculate the average amount spent per day, avoiding division by zero
        val dailyAverage = if (daysDiff > 0) totalSpent / daysDiff else 0.0

        // Update the UI elements to show the total amount spent and daily average in currency format
        tvTotalSpent.text = formatCurrency(totalSpent)
        tvDailyAverage.text = formatCurrency(dailyAverage)

        // Check if a budget goal is currently set
        currentBudgetGoal?.let { goal ->
            // Format the minimum and maximum goal values for display
            val min = formatCurrency(goal.minGoalAmount)
            val max = formatCurrency(goal.maxGoalAmount)

            // Display the budget range in the format: "Rmin - Rmax"
            tvBudgetGoal.text = "$min - $max"

            // Calculate the adjusted maximum budget for the current period
            val periodBudget = if (isCustomDateRange) {
                // For custom ranges, proportionally scale the max goal by the number of days selected

                // Calculate the total number of days defined in the full budget goal
                val totalDays =
                    ((goal.getEndDateAsDate().time - goal.getStartDateAsDate().time) / (1000 * 60 * 60 * 24)).toInt()

                // Scale factor = selected range / total budget duration
                val scaleFactor = daysDiff.toDouble() / totalDays

                // Adjust the max goal by the scale factor
                goal.maxGoalAmount * scaleFactor
            } else {
                // For standard periods, adjust the budget according to predefined logic
                when (currentPeriod) {
                    TimePeriod.WEEK -> goal.maxGoalAmount / 4.3  // Approximate weeks in a month
                    TimePeriod.MONTH -> goal.maxGoalAmount        // Full monthly budget
                    TimePeriod.YEAR -> goal.maxGoalAmount * 12    // Annual budget is 12x the monthly
                    TimePeriod.CUSTOM -> goal.maxGoalAmount       // Fallback in case CUSTOM is somehow hit
                }
            }

            // Determine budget status: Under Budget, On Track, or Over Budget
            val budgetStatus = when {
                // Case 1: Spending is less than scaled minimum budget – well under control
                totalSpent <= goal.minGoalAmount * (daysDiff / 30.0) -> {
                    tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.olivine))
                    "Under Budget"
                }
                // Case 2: Spending is within the scaled max budget – acceptable
                totalSpent <= periodBudget -> {
                    tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.celadon))
                    "On Track"
                }
                // Case 3: Spending exceeds the scaled max budget – over budget
                else -> {
                    tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.coral_pink))
                    "Over Budget"
                }
            }

            // Display the calculated budget status in the UI
            tvBudgetStatus.text = budgetStatus

        } ?: run {
            // If no budget goal is set, indicate this clearly in the UI

            // Show fallback message for budget range
            tvBudgetGoal.text = "No budget set"

            // Show fallback message and dimmed color for budget status
            tvBudgetStatus.text = "N/A"
            tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary))
        }
    }


    /**
     * Updates the chart displayed on the UI based on the selected chart type and available data.
     * It handles three types of charts: Line, Pie, and Bar.
     * If there is no expense data, it shows a fallback message instead.
     */
    private fun updateChart() {
        // If there are no expenses available to visualize, show a 'No Data' view and exit the function early
        if (currentExpenses.isEmpty()) {
            showNoData()
            return
        }

        // Hide all chart views initially to ensure only the selected chart will be visible
        hideAllCharts()

        // Determine which chart type to show based on user selection
        when (currentChartType) {
            ChartType.LINE -> {
                // If the selected chart type is a LINE chart:
                // 1. Make the LineChart view visible
                lineChart.visibility = View.VISIBLE

                // 2. Update the chart title to reflect a cumulative spending trend
                tvChartTitle.text = "📈 Cumulative Spending Trend"

                // 3. Prepare and render the data into the line chart using an enhanced setup method
                setupImprovedLineChart()

                // 4. Show the legend (e.g., budget goal indicator) if a budget goal has been set by the user
                chartLegend.visibility = if (currentBudgetGoal != null) View.VISIBLE else View.GONE
            }

            ChartType.PIE -> {
                // If the selected chart type is a PIE chart:
                // 1. Make the PieChart view visible
                pieChart.visibility = View.VISIBLE

                // 2. Update the chart title to reflect an expense category breakdown
                tvChartTitle.text = "🥧 Category Breakdown"

                // 3. Render the pie chart with improved settings and visuals
                setupImprovedPieChart()

                // 4. Hide the legend since it’s usually not needed in pie charts
                chartLegend.visibility = View.GONE
            }

            ChartType.BAR -> {
                // If the selected chart type is a BAR chart:
                // 1. Make the BarChart view visible
                barChart.visibility = View.VISIBLE

                // 2. Update the chart title to reflect a category-by-category comparison
                tvChartTitle.text = "📊 Category Comparison"

                // 3. Set up and display the bar chart with formatted data
                setupImprovedBarChart()

                // 4. Hide the legend as bar charts generally use axes for value context
                chartLegend.visibility = View.GONE
            }
        }
    }

    /**
     * Hides all chart views and the 'No Data' layout from the screen.
     * This function is used as a common reset method to ensure the UI starts in a clean state
     * before showing any specific chart or the no-data message.
     */
    private fun hideAllCharts() {
        // Hide the line chart view
        lineChart.visibility = View.GONE

        // Hide the pie chart view
        pieChart.visibility = View.GONE

        // Hide the bar chart view
        barChart.visibility = View.GONE

        // Hide the layout that displays a 'No Data' message
        noDataLayout.visibility = View.GONE
    }

    /**
     * Displays the 'No Data' layout to inform the user that there are no expenses
     * to generate a chart from. This is typically called when the expenses list is empty.
     */
    private fun showNoData() {
        // First, hide all chart views to prevent overlap with the no-data message
        hideAllCharts()

        // Make the 'No Data' layout visible to the user
        noDataLayout.visibility = View.VISIBLE

        // Update the chart title to reflect that there is no data available for display
        tvChartTitle.text = "📊 No Data Available"
    }

    /**
     * Controls the visibility of the loading indicator (progress bar).
     * This function is called whenever the app is fetching data, such as expenses or budget goals.
     *
     * parameter show A boolean flag indicating whether to show (true) or hide (false) the loading spinner.
     */
    private fun showLoading(show: Boolean) {
        // Show the progress bar if 'show' is true, otherwise hide it
        progressBar.visibility = if (show) View.VISIBLE else View.GONE

        // If we are showing the loading spinner, also hide all charts to clear the screen
        if (show) hideAllCharts()
    }


    /**
     * Prepares and displays a cumulative line chart that visualizes the user's total spending over time.
     * The chart adapts based on selected time periods and dynamically adds goal lines if a budget is set.
     */
    private fun setupImprovedLineChart() {
        // A list of chart data points where each Entry represents cumulative spending at a given time unit (day/week/month)
        val entries = mutableListOf<Entry>()

        // Retrieve the currently selected date range (start and end) to know the time span of the expenses
        val (startDate, endDate) = getDateRange()

        // Log the number of expenses being visualized for debugging purposes
        Log.d(TAG, "Setting up line chart with ${currentExpenses.size} expenses")

        // Determine how to group expenses (by day, week, month, or quarter) based on the selected time range
        val (groupedData, labels) = when {
            // If the user selected a custom date range
            isCustomDateRange -> {
                // Calculate the number of days between the start and end date
                val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()

                // Choose grouping granularity based on how long the date range is
                when {
                    daysDiff <= 7 -> groupByDays(startDate, endDate)      // Short range: show daily
                    daysDiff <= 60 -> groupByWeeks(
                        startDate,
                        endDate
                    )    // Medium range: show weekly
                    else -> groupByMonths(
                        startDate,
                        endDate
                    )             // Long range: show monthly
                }
            }

            // Predefined time period cases (not custom range)
            currentPeriod == TimePeriod.WEEK -> groupByDays(startDate, endDate)
            currentPeriod == TimePeriod.MONTH -> groupByWeeks(startDate, endDate)
            currentPeriod == TimePeriod.YEAR -> groupByQuarters(startDate, endDate)
            else -> groupByDays(startDate, endDate) // Fallback: group by day
        }

        // Log the result of data grouping for validation
        Log.d(TAG, "Grouped data: ${groupedData.size} periods, Labels: $labels")

        // Initialize cumulative total to build a progressive sum of expenses
        var cumulativeTotal = 0.0

        // Iterate over grouped expense data and convert it into chart entries
        groupedData.forEachIndexed { index, amount ->
            cumulativeTotal += amount // Add current period's expense to running total
            entries.add(
                Entry(
                    index.toFloat(),
                    cumulativeTotal.toFloat()
                )
            ) // Create chart data point
            Log.d(TAG, "Entry $index: Amount = $amount, Cumulative = $cumulativeTotal")
        }

        // If there is no data to display, show 'No Data' message and return early
        if (entries.isEmpty()) {
            Log.w(TAG, "No entries to plot")
            showNoData()
            return
        }

        // Create the dataset that will be used to render the line on the chart
        val dataSet = LineDataSet(entries, "Cumulative Spending").apply {
            color = ContextCompat.getColor(this@AnalyticsActivity, R.color.celadon) // Line color
            setCircleColor(
                ContextCompat.getColor(
                    this@AnalyticsActivity,
                    R.color.asparagus
                )
            ) // Circle marker color
            lineWidth = 3f
            circleRadius = 6f
            setCircleHoleColor(Color.WHITE)
            circleHoleRadius = 3f
            setDrawFilled(true) // Fill area under the line with gradient
            fillDrawable =
                ContextCompat.getDrawable(this@AnalyticsActivity, R.drawable.line_chart_gradient)
            valueTextSize = 0f // Hide values on each point
            setDrawValues(false)
            isHighlightEnabled = true // Allow line to highlight when touched
            highlightLineWidth = 2f
            setDrawHighlightIndicators(true)
            mode = LineDataSet.Mode.LINEAR // Smooth line (can be CUBIC_BEZIER for curves)
        }

        // Bind the dataset to the chart
        lineChart.data = LineData(dataSet)

        // Configure the X-axis to use custom labels (e.g., "Week 1", "Feb", etc.)
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Customize X-axis scaling and spacing to fit all labels correctly
        lineChart.xAxis.apply {
            granularity = 1f // One unit between labels
            setLabelCount(labels.size, false) // Let chart auto-balance label spacing
            axisMinimum = 0f
            axisMaximum = (labels.size - 1).toFloat()
        }

        // Compute the maximum Y value to determine appropriate Y-axis range
        val maxValue = if (cumulativeTotal > 0) cumulativeTotal.toFloat() else 1000f

        // Round up the Y-axis maximum based on spending amount
        val yAxisMax = when {
            maxValue <= 500 -> 500f
            maxValue <= 1000 -> 1000f
            maxValue <= 2000 -> 2000f
            maxValue <= 5000 -> 5000f
            maxValue <= 10000 -> 10000f
            else -> ((maxValue / 5000).toInt() + 1) * 5000f // Round up to next 5000
        }

        // Apply calculated Y-axis range and label count
        lineChart.axisLeft.apply {
            axisMaximum = yAxisMax
            axisMinimum = 0f
            setLabelCount(6, false)
        }

        // Remove any old goal lines from previous chart updates
        lineChart.axisLeft.removeAllLimitLines()

        // If a budget goal is available, overlay horizontal lines to show min and max thresholds
        currentBudgetGoal?.let { goal ->
            Log.d(TAG, "Budget Goal - Min: ${goal.minGoalAmount}, Max: ${goal.maxGoalAmount}")

            val minGoalForPeriod = goal.minGoalAmount.toFloat()
            val maxGoalForPeriod = goal.maxGoalAmount.toFloat()

            Log.d(TAG, "Plotting goal lines at Min: $minGoalForPeriod, Max: $maxGoalForPeriod")

            // Add dashed line to indicate minimum budget goal
            val minLine = LimitLine(
                minGoalForPeriod,
                "Min Goal (${formatCurrency(goal.minGoalAmount)})"
            ).apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.olivine)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 9f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.olivine)
            }

            // Add dashed line to indicate maximum budget goal
            val maxLine = LimitLine(
                maxGoalForPeriod,
                "Max Goal (${formatCurrency(goal.maxGoalAmount)})"
            ).apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.coral_pink)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 9f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.coral_pink)
            }

            // Add limit lines to Y-axis and ensure chart height accommodates them
            lineChart.axisLeft.apply {
                addLimitLine(minLine)
                addLimitLine(maxLine)

                // Adjust Y-axis max to ensure goal lines fit within view
                axisMaximum = maxOf(yAxisMax, maxGoalForPeriod + 500f)
            }
        }

        // Final step: refresh the chart UI with new data
        Log.d(TAG, "Line chart setup complete with ${entries.size} entries")
        lineChart.invalidate()
    }

    /**
     * Groups expenses by individual days between the given start and end dates.
     *
     * parameter startDate The start date of the range.
     * parameter endDate The end date of the range.
     * returns A Pair containing a list of daily expense totals and a list of corresponding day labels (e.g., Mon, Tue).
     */
    private fun groupByDays(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>() // Stores total expense amounts per day
        val labels = mutableListOf<String>()  // Stores corresponding day labels for the X-axis
        val expensesByDate = mutableMapOf<String, Double>() // Maps each date to its total expense

        // Group expenses by date string in "yyyy-MM-dd" format
        currentExpenses.forEach { expense ->
            val calendar = Calendar.getInstance()
            calendar.time = expense.getExpenseDateAsDate()

            // Format the expense date as a string key
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            // Add the expense total to the existing total for that date, or initialize it
            expensesByDate[dateKey] =
                expensesByDate.getOrDefault(dateKey, 0.0) + expense.totalAmount
        }

        // Prepare calendar and formatter for looping through the date range
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // E.g., Mon, Tue

        // Iterate from startDate to endDate, day by day
        while (calendar.time <= endDate) {
            // Format the current date as a key for lookup
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            // Get the total expense for the current day, or 0.0 if none
            val totalForDate = expensesByDate[dateKey] ?: 0.0

            // Add the total and corresponding label to the result lists
            amounts.add(totalForDate)
            labels.add(dayFormat.format(calendar.time))

            Log.d(TAG, "Day ${dayFormat.format(calendar.time)}: R$totalForDate")

            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        Log.d(TAG, "Days grouping: ${amounts.size} days, total expenses: ${amounts.sum()}")

        // Return a pair of the daily amounts and corresponding day labels
        return Pair(amounts, labels)
    }


    /**
     * Groups expenses by week within the specified date range.
     *
     * parameter startDate The start date of the analysis period.
     * parameter endDate The end date of the analysis period.
     * returns A Pair containing a list of weekly total expenses and a list of corresponding week labels (e.g., "Week 1").
     */
    private fun groupByWeeks(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>() // Holds the total expense amount per week
        val labels = mutableListOf<String>()  // Holds week labels like "Week 1", "Week 2", etc.

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        // Align the calendar to the start of the week based on the locale (usually Sunday or Monday)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        var weekNumber = 1 // Counter to keep track of which week we’re processing

        // Loop through the date range in weekly intervals
        while (calendar.time <= endDate) {
            val weekStart = calendar.time // Start date of the current week

            // Move the calendar forward by 6 days to get the end of the week
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val weekEnd =
                if (calendar.time > endDate) endDate else calendar.time // Don't go past the endDate

            // Filter the expenses that fall within this week range
            val weekTotal = currentExpenses.filter { expense ->
                val expenseDate = expense.getExpenseDateAsDate()
                expenseDate >= weekStart && expenseDate <= weekEnd
            }.sumOf { it.totalAmount } // Sum the total amounts for this week

            amounts.add(weekTotal) // Add the weekly total to the list
            labels.add("Week $weekNumber") // Add a label for this week

            Log.d(TAG, "Week $weekNumber: R$weekTotal") // Log the week’s total for debugging

            // Advance calendar by one day to start the next week's range
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            weekNumber++
        }

        Log.d(TAG, "Weeks grouping: ${amounts.size} weeks, total expenses: ${amounts.sum()}")

        // Return the pair of amounts and their corresponding week labels
        return Pair(amounts, labels)
    }

    /**
     * Groups expenses by calendar quarters within the specified date range.
     *
     * parameter startDate The start date of the analysis period.
     * parameter endDate The end date of the analysis period.
     * returns A Pair containing a list of total expenses per quarter and a list of corresponding quarter labels (e.g., "Q1", "Q2").
     */
    private fun groupByQuarters(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>() // Holds the total expense amount for each quarter
        val labels = mutableListOf<String>()  // Holds quarter labels like "Q1", "Q2", etc.

        val calendar = Calendar.getInstance()
        calendar.time = startDate // Set calendar to the start date to get the year for grouping

        // Loop through the four quarters of the year: 1 to 4
        for (quarter in 1..4) {
            // Define the start date/time for the current quarter
            val quarterStart = Calendar.getInstance().apply {
                set(
                    Calendar.YEAR,
                    calendar.get(Calendar.YEAR)
                )              // Set to the year of the start date
                set(
                    Calendar.MONTH,
                    (quarter - 1) * 3
                )                       // Set month to the first month of the quarter (0-based)
                set(
                    Calendar.DAY_OF_MONTH,
                    1
                )                                // First day of the quarter
                set(
                    Calendar.HOUR_OF_DAY,
                    0
                )                                 // Reset hour to start of day
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Define the end date/time for the current quarter
            val quarterEnd = Calendar.getInstance().apply {
                set(
                    Calendar.YEAR,
                    calendar.get(Calendar.YEAR)
                )              // Same year as start date
                set(
                    Calendar.MONTH,
                    quarter * 3 - 1
                )                         // Last month of the quarter (0-based)
                set(
                    Calendar.DAY_OF_MONTH,
                    getActualMaximum(Calendar.DAY_OF_MONTH)
                ) // Last day of that month
                set(
                    Calendar.HOUR_OF_DAY,
                    23
                )                                // End of the day (23:59:59.999)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            // Filter expenses whose dates fall within the current quarter range
            val quarterTotal = currentExpenses.filter { expense ->
                val expenseDate = expense.getExpenseDateAsDate()
                expenseDate >= quarterStart.time && expenseDate <= quarterEnd.time
            }.sumOf { it.totalAmount } // Sum total amounts for all filtered expenses

            amounts.add(quarterTotal)        // Add the total expenses of the quarter to the list
            labels.add("Q$quarter")          // Add a label for the quarter (e.g., "Q1")
        }

        // Return the pair: list of quarter totals and corresponding labels
        return Pair(amounts, labels)
    }


    /**
     * Groups expenses by month within the specified date range.
     *
     * @param startDate The start date of the period to analyze.
     * @param endDate The end date of the period to analyze.
     * @return A Pair containing a list of total expenses per month and a list of corresponding month labels (e.g., "Jan", "Feb").
     */
    private fun groupByMonths(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>() // List to hold total expenses per month
        val labels = mutableListOf<String>()  // List to hold month labels as strings

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        // Reset calendar to the very start of the start month (first day, midnight)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val monthFormat = SimpleDateFormat(
            "MMM",
            Locale.getDefault()
        ) // Format to display abbreviated month names, e.g., "Jan"

        // Loop over each month until calendar date passes the endDate
        while (calendar.time <= endDate) {
            val monthStart = calendar.time // Mark the start of the current month

            calendar.add(
                Calendar.MONTH,
                1
            )       // Move calendar to the first day of the next month
            calendar.add(
                Calendar.DAY_OF_MONTH,
                -1
            ) // Step back one day to get the last day of the current month

            // Determine the actual month end date, ensure it does not exceed the provided endDate
            val monthEnd = if (calendar.time > endDate) endDate else calendar.time

            // Filter all expenses that fall within the current month's date range and sum their amounts
            val monthTotal = currentExpenses.filter { expense ->
                val expenseDate = expense.getExpenseDateAsDate()
                expenseDate >= monthStart && expenseDate <= monthEnd
            }.sumOf { it.totalAmount }

            amounts.add(monthTotal)              // Add the total for the current month to the amounts list
            labels.add(monthFormat.format(monthStart)) // Add the formatted month label (e.g., "Jan") to labels list

            calendar.add(
                Calendar.DAY_OF_MONTH,
                1
            ) // Move calendar forward to the first day of the next month for the next iteration
        }

        // Return the pair of lists: amounts by month and their corresponding labels
        return Pair(amounts, labels)
    }

    /**
     * Sets up and configures the pie chart to display expense data grouped by category.
     * The pie chart shows only categories that represent at least 0.5% of total expenses.
     * Category names are removed from the chart slices to reduce clutter, and a custom legend
     * with category names and percentages is displayed below the chart.
     */
    private fun setupImprovedPieChart() {
        // Group all expenses by their category into a map: category -> list of expenses
        val expensesByCategory = currentExpenses.groupBy { it.category }

        // Calculate the total amount of all expenses combined
        val totalAmount = currentExpenses.sumOf { it.totalAmount }

        // If there are no expenses, show a "no data" state and exit early
        if (totalAmount == 0.0) {
            showNoData()
            return
        }

        val entries = mutableListOf<PieEntry>() // List to hold PieEntries for the chart slices
        val colors =
            mutableListOf<Int>()       // List to hold colors for the slices (not used here directly)

        // Sort categories descending by their total expense amount
        val sortedCategories = expensesByCategory.entries.sortedByDescending {
            it.value.sumOf { expense -> expense.totalAmount }
        }

        // Iterate over sorted categories to prepare pie chart slices
        sortedCategories.forEach { (category, expenses) ->
            val categoryTotal =
                expenses.sumOf { it.totalAmount }      // Sum total for this category
            val percentage =
                (categoryTotal / totalAmount * 100).toFloat() // Calculate category percentage of total

            if (percentage >= 0.5f) {  // Only include categories contributing at least 0.5%
                // Add PieEntry without label to avoid clutter on chart slices
                entries.add(PieEntry(categoryTotal.toFloat()))
            }
        }

        // Define a set of custom colors for the pie slices
        val customColors = listOf(
            ContextCompat.getColor(this, R.color.celadon),
            ContextCompat.getColor(this, R.color.asparagus),
            ContextCompat.getColor(this, R.color.olivine),
            ContextCompat.getColor(this, R.color.coral_pink),
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#607D8B")  // Blue Grey
        )

        // Create and configure the PieDataSet using the entries and custom colors
        val dataSet = PieDataSet(entries, "").apply {
            setColors(customColors)              // Apply the custom colors to the dataset
            valueTextColor = Color.TRANSPARENT   // Hide the value text on slices completely
            valueTextSize = 0f                   // Set value text size to zero (no text)
            setDrawValues(false)                 // Disable drawing values on slices entirely
            sliceSpace = 2f                     // Set space between slices
            selectionShift = 8f                 // Set how far a slice moves when selected
        }

        // Configure the pie chart UI settings related to labels and appearance
        pieChart.apply {
            setDrawEntryLabels(false)            // Do not show category labels directly on the pie slices
            setEntryLabelColor(Color.TRANSPARENT) // Make labels fully transparent as a backup
            setEntryLabelTextSize(0f)            // Set label text size to zero to hide labels
        }

        // Configure the legend to be displayed below the pie chart with custom labels and colors
        pieChart.legend.apply {
            isEnabled = true                     // Enable legend display
            orientation =
                com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            verticalAlignment =
                com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment =
                com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
            textSize = 10f
            isWordWrapEnabled = true             // Allow wrapping if legend labels are too long

            val legendLabels = mutableListOf<String>()
            val categoriesWithPercentages = mutableListOf<Pair<String, Float>>()

            // Rebuild category + percentage pairs to show in legend, matching the filtered chart slices
            sortedCategories.forEach { (category, expenses) ->
                val categoryTotal = expenses.sumOf { it.totalAmount }
                val percentage = (categoryTotal / totalAmount * 100).toFloat()

                if (percentage >= 0.5f) {
                    categoriesWithPercentages.add(Pair(category, percentage))
                }
            }

            // Create legend labels with category name and percentage (rounded to integer %)
            categoriesWithPercentages.forEachIndexed { index, (category, percentage) ->
                legendLabels.add("$category (${percentage.toInt()}%)")
            }

            // Assign the custom legend entries to the legend, mapping labels and corresponding colors
            setCustom(legendLabels.mapIndexed { index, label ->
                com.github.mikephil.charting.components.LegendEntry().apply {
                    this.label = label
                    formColor = customColors[index % customColors.size] // Cycle through colors
                }
            })
        }

        // Set the prepared data to the pie chart and refresh it
        pieChart.data = PieData(dataSet)
        pieChart.highlightValues(null) // Clear any previous highlights
        pieChart.invalidate()          // Redraw the chart with updated data
    }


    /**
     * Sets up and configures a bar chart displaying total expenses grouped by category.
     * Shows the top 10 categories sorted by total spending.
     * Custom colors are applied to bars and legend entries.
     * Adds optional budget goal limit lines on the left Y-axis.
     */
    private fun setupImprovedBarChart() {
        // Group all expenses by category: Map<Category, List<Expense>>
        val expensesByCategory = currentExpenses.groupBy { it.category }

        val entries =
            mutableListOf<BarEntry>()      // Holds the bar entries (index, value) for the chart
        val categoryLabels = mutableListOf<String>() // Stores category names for legend labels
        val colors =
            mutableListOf<Int>()             // Stores colors corresponding to each category/bar

        // Sort categories by total spending descending and take only the top 10 categories
        val sortedCategories = expensesByCategory.entries
            .sortedByDescending { it.value.sumOf { expense -> expense.totalAmount } }
            .take(10)

        // Define custom colors for the bars to maintain visual consistency with pie chart colors
        val customColors = listOf(
            ContextCompat.getColor(this, R.color.celadon),
            ContextCompat.getColor(this, R.color.asparagus),
            ContextCompat.getColor(this, R.color.olivine),
            ContextCompat.getColor(this, R.color.coral_pink),
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#607D8B")  // Blue Grey
        )

        // Build bar entries, category labels, and assign colors for each top category
        sortedCategories.forEachIndexed { index, (category, expenses) ->
            val total = expenses.sumOf { it.totalAmount }       // Sum total expenses for category
            entries.add(
                BarEntry(
                    index.toFloat(),
                    total.toFloat()
                )
            ) // Create bar entry (x=index, y=total)
            categoryLabels.add(category)                          // Add category label for legend
            colors.add(customColors[index % customColors.size])  // Cycle through colors for each bar
        }

        // Create BarDataSet with the entries and configure appearance and value formatting
        val dataSet = BarDataSet(entries, "Category Spending").apply {
            setColors(colors)    // Set colors of bars
            valueTextColor = ContextCompat.getColor(
                this@AnalyticsActivity,
                R.color.text_primary
            ) // Value text color
            valueTextSize = 10f  // Size of value text on bars

            // Custom value formatter to display amounts with 'R' currency and abbreviated 'k' for thousands
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) {
                        when {
                            value >= 1000 -> "R${(value / 1000).toInt()}k"  // Display in thousands with 'k' suffix
                            else -> "R${value.toInt()}"                       // Display normal value with 'R'
                        }
                    } else ""
                }
            }
        }

        // Configure the bar chart legend to show category names with corresponding colors
        barChart.legend.apply {
            isEnabled = true                      // Show legend
            orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
            verticalAlignment =
                com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.CENTER
            horizontalAlignment =
                com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
            textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
            textSize = 10f

            // Create custom legend entries with truncated category labels (max 12 chars)
            setCustom(categoryLabels.mapIndexed { index, label ->
                com.github.mikephil.charting.components.LegendEntry().apply {
                    this.label = if (label.length > 12) "${label.take(12)}..." else label
                    formColor = colors[index]
                }
            })
        }

        // Assign the prepared dataset to the bar chart
        barChart.data = BarData(dataSet)

        // Hide X-axis labels since category info is shown in the legend instead
        barChart.xAxis.setDrawLabels(false)

        // Remove any existing limit lines on the left Y-axis before adding new ones
        barChart.axisLeft.removeAllLimitLines()

        // If a budget goal is set, add dashed limit lines indicating min and max budget goals
        currentBudgetGoal?.let { goal ->
            val minLine = LimitLine(goal.minGoalAmount.toFloat(), "Min Budget").apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.olivine)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)               // Dashed line style
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 9f
            }

            val maxLine = LimitLine(goal.maxGoalAmount.toFloat(), "Max Budget").apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.coral_pink)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 9f
            }

            // Add the limit lines to the left Y-axis
            barChart.axisLeft.apply {
                addLimitLine(minLine)
                addLimitLine(maxLine)
            }
        }

        // Refresh the chart to reflect all changes
        barChart.invalidate()
    }

    /**
     * Helper function to format currency amounts consistently as South African Rand (R).
     */
    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("ZAR", "R")
    }
}