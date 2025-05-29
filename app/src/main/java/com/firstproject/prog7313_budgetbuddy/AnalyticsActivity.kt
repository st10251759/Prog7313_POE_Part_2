package com.firstproject.prog7313_budgetbuddy

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

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var btnRefresh: ImageButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analytics)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        initializeUI()
        setupListeners()
        loadData()
    }

    private fun initializeUI() {
        btnBack = findViewById(R.id.btnBack)
        btnRefresh = findViewById(R.id.btnRefresh)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvDailyAverage = findViewById(R.id.tvDailyAverage)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        tvBudgetGoal = findViewById(R.id.tvBudgetGoal)
        tvChartTitle = findViewById(R.id.tvChartTitle)
        tvSelectedDateRange = findViewById(R.id.tvSelectedDateRange)
        progressBar = findViewById(R.id.progressBar)
        noDataLayout = findViewById(R.id.noDataLayout)
        chartLegend = findViewById(R.id.chartLegend)

        // Date filter components
        etFromDate = findViewById(R.id.etFromDate)
        etToDate = findViewById(R.id.etToDate)
        customDateLayout = findViewById(R.id.customDateLayout)

        // Period buttons
        btnWeekPeriod = findViewById(R.id.btnWeekPeriod)
        btnMonthPeriod = findViewById(R.id.btnMonthPeriod)
        btnYearPeriod = findViewById(R.id.btnYearPeriod)
        btnCustomPeriod = findViewById(R.id.btnCustomPeriod)

        // Chart type buttons
        btnLineChart = findViewById(R.id.btnLineChart)
        btnPieChart = findViewById(R.id.btnPieChart)
        btnBarChart = findViewById(R.id.btnBarChart)

        // Charts
        lineChart = findViewById(R.id.lineChart)
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)

        setupCharts()
        updateSelectedDateRange()
    }

    private fun setupCharts() {
        // Setup Line Chart with better styling
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            legend.isEnabled = false

            // Styling
            setBackgroundColor(Color.TRANSPARENT)
            setBorderColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.asparagus))
            setBorderWidth(1f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                gridLineWidth = 0.5f
                granularity = 1f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
                setAvoidFirstLastClipping(true)
            }

            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                gridLineWidth = 0.5f
                setDrawZeroLine(true)
                zeroLineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_tertiary)
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
                setDrawAxisLine(false)
                axisMinimum = 0f

                // Format Y-axis values as currency
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "R${value.toInt()}"
                    }
                }
            }

            // Enable data point highlighting
            isHighlightPerDragEnabled = true
            isHighlightPerTapEnabled = true
        }

        // Setup Pie Chart with better styling
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.text_primary))
            setEntryLabelTextSize(11f)
            centerText = "Spending\nBreakdown"
            setCenterTextSize(14f)
            setCenterTextColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.text_primary))
            holeRadius = 45f
            transparentCircleRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            legend.apply {
                isEnabled = true
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
            }

            // Animation
            animateY(1000)
        }

        // Setup Bar Chart with better styling
        barChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            legend.isEnabled = false
            setFitBars(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
                labelRotationAngle = -45f
                setAvoidFirstLastClipping(true)
            }

            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                gridLineWidth = 0.5f
                setDrawZeroLine(false)
                axisMinimum = 0f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f

                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "R${value.toInt()}"
                    }
                }
            }

            // Animation
            animateY(1000)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { loadData() }

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

    private fun selectPeriod(period: TimePeriod) {
        currentPeriod = period
        isCustomDateRange = false
        customDateLayout.visibility = View.GONE
        updatePeriodButtons()
        updateSelectedDateRange()
        loadData()
    }

    private fun selectCustomPeriod() {
        isCustomDateRange = true
        customDateLayout.visibility = View.VISIBLE
        updatePeriodButtons()

        // Set default custom dates if not set
        if (customStartDate == null) {
            customStartDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
            }
        }
        if (customEndDate == null) {
            customEndDate = Calendar.getInstance()
        }

        updateCustomDateFields()
        updateSelectedDateRange()
        loadData()
    }

    private fun selectChartType(chartType: ChartType) {
        currentChartType = chartType
        updateChartTypeButtons()
        updateChart()
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = if (isStartDate) {
            customStartDate ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
        } else {
            customEndDate ?: Calendar.getInstance()
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                    if (isStartDate) {
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

                if (isStartDate) {
                    customStartDate = selectedCalendar
                } else {
                    customEndDate = selectedCalendar
                }

                updateCustomDateFields()
                updateSelectedDateRange()
                loadData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateCustomDateFields() {
        customStartDate?.let { etFromDate.setText(dateFormat.format(it.time)) }
        customEndDate?.let { etToDate.setText(dateFormat.format(it.time)) }
    }

    private fun updateSelectedDateRange() {
        val text = if (isCustomDateRange && customStartDate != null && customEndDate != null) {
            "${dateFormat.format(customStartDate!!.time)} - ${dateFormat.format(customEndDate!!.time)}"
        } else {
            when (currentPeriod) {
                TimePeriod.WEEK -> "Last 7 days"
                TimePeriod.MONTH -> "Last 30 days"
                TimePeriod.YEAR -> "Last 365 days"
                TimePeriod.CUSTOM -> "Custom range"
            }
        }
        tvSelectedDateRange.text = text
    }

    private fun updatePeriodButtons() {
        // Reset all buttons
        listOf(btnWeekPeriod, btnMonthPeriod, btnYearPeriod, btnCustomPeriod).forEach { button ->
            button.background = null
            button.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        }

        // Set selected button
        val selectedButton = if (isCustomDateRange) {
            btnCustomPeriod
        } else {
            when (currentPeriod) {
                TimePeriod.WEEK -> btnWeekPeriod
                TimePeriod.MONTH -> btnMonthPeriod
                TimePeriod.YEAR -> btnYearPeriod
                TimePeriod.CUSTOM -> btnCustomPeriod
            }
        }
        selectedButton.background = ContextCompat.getDrawable(this, R.drawable.period_toggle_selected)
        selectedButton.setTextColor(Color.WHITE)
    }

    private fun updateChartTypeButtons() {
        // Reset all buttons
        listOf(btnLineChart, btnPieChart, btnBarChart).forEach { button ->
            button.background = null
            button.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        }

        // Set selected button
        val selectedButton = when (currentChartType) {
            ChartType.LINE -> btnLineChart
            ChartType.PIE -> btnPieChart
            ChartType.BAR -> btnBarChart
        }
        selectedButton.background = ContextCompat.getDrawable(this, R.drawable.period_toggle_selected)
        selectedButton.setTextColor(Color.WHITE)
    }

    private fun loadData() {
        val userId = auth.currentUser?.uid ?: return

        showLoading(true)

        val (startDate, endDate) = getDateRange()

        Log.d(TAG, "Loading data from $startDate to $endDate")

        // Load expenses for the period
        viewModel.getExpensesByPeriod(userId, startDate, endDate).observe(this) { expenses ->
            Log.d(TAG, "Loaded ${expenses?.size ?: 0} expenses")
            currentExpenses = expenses ?: emptyList()

            // Load budget goal
            viewModel.getCurrentBudgetGoal(userId).observe(this) { budgetGoal ->
                Log.d(TAG, "Loaded budget goal: $budgetGoal")
                currentBudgetGoal = budgetGoal
                updateUI()
                showLoading(false)
            }
        }
    }

    private fun getDateRange(): Pair<Date, Date> {
        return if (isCustomDateRange && customStartDate != null && customEndDate != null) {
            Pair(customStartDate!!.time, customEndDate!!.time)
        } else {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -currentPeriod.days)
            val startDate = calendar.time
            Pair(startDate, endDate)
        }
    }

    private fun updateUI() {
        updateSummary()
        updateChart()
    }

    private fun updateSummary() {
        val totalSpent = currentExpenses.sumOf { it.totalAmount }
        val (startDate, endDate) = getDateRange()
        val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
        val dailyAverage = if (daysDiff > 0) totalSpent / daysDiff else 0.0

        // Update displays
        tvTotalSpent.text = formatCurrency(totalSpent)
        tvDailyAverage.text = formatCurrency(dailyAverage)

        // Update budget status
        currentBudgetGoal?.let { goal ->
            val min = formatCurrency(goal.minGoalAmount)
            val max = formatCurrency(goal.maxGoalAmount)
            tvBudgetGoal.text = "$min - $max"

            // Calculate budget status
            val periodBudget = if (isCustomDateRange) {
                // For custom periods, scale the budget proportionally
                val totalDays = ((goal.getEndDateAsDate().time - goal.getStartDateAsDate().time) / (1000 * 60 * 60 * 24)).toInt()
                val scaleFactor = daysDiff.toDouble() / totalDays
                goal.maxGoalAmount * scaleFactor
            } else {
                when (currentPeriod) {
                    TimePeriod.WEEK -> goal.maxGoalAmount / 4.3 // Approximate weeks in a month
                    TimePeriod.MONTH -> goal.maxGoalAmount
                    TimePeriod.YEAR -> goal.maxGoalAmount * 12
                    TimePeriod.CUSTOM -> goal.maxGoalAmount // Added CUSTOM case
                }
            }

            val budgetStatus = when {
                totalSpent <= goal.minGoalAmount * (daysDiff / 30.0) -> {
                    tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.olivine))
                    "Under Budget"
                }
                totalSpent <= periodBudget -> {
                    tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.celadon))
                    "On Track"
                }
                else -> {
                    tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.coral_pink))
                    "Over Budget"
                }
            }
            tvBudgetStatus.text = budgetStatus
        } ?: run {
            tvBudgetGoal.text = "No budget set"
            tvBudgetStatus.text = "N/A"
            tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary))
        }
    }

    private fun updateChart() {
        if (currentExpenses.isEmpty()) {
            showNoData()
            return
        }

        hideAllCharts()

        when (currentChartType) {
            ChartType.LINE -> {
                lineChart.visibility = View.VISIBLE
                tvChartTitle.text = "📈 Spending Trend"
                setupLineChart()
                chartLegend.visibility = if (currentBudgetGoal != null) View.VISIBLE else View.GONE
            }
            ChartType.PIE -> {
                pieChart.visibility = View.VISIBLE
                tvChartTitle.text = "🥧 Category Breakdown"
                setupPieChart()
                chartLegend.visibility = View.GONE
            }
            ChartType.BAR -> {
                barChart.visibility = View.VISIBLE
                tvChartTitle.text = "📊 Category Comparison"
                setupBarChart()
                chartLegend.visibility = if (currentBudgetGoal != null) View.VISIBLE else View.GONE
            }
        }
    }

    private fun hideAllCharts() {
        lineChart.visibility = View.GONE
        pieChart.visibility = View.GONE
        barChart.visibility = View.GONE
        noDataLayout.visibility = View.GONE
    }

    private fun showNoData() {
        hideAllCharts()
        noDataLayout.visibility = View.VISIBLE
        tvChartTitle.text = "📊 No Data Available"
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) hideAllCharts()
    }

    private fun setupLineChart() {
        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        // Group expenses by date
        val (startDate, endDate) = getDateRange()
        val expensesByDate = currentExpenses.groupBy { expense ->
            val calendar = Calendar.getInstance()
            calendar.time = expense.getExpenseDateAsDate()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        }

        // Determine date format and increment based on period
        val (dateFormat, increment, incrementValue) = when {
            isCustomDateRange -> {
                val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
                when {
                    daysDiff <= 7 -> Triple(SimpleDateFormat("EEE", Locale.getDefault()), Calendar.DAY_OF_YEAR, 1)
                    daysDiff <= 60 -> Triple(SimpleDateFormat("MMM dd", Locale.getDefault()), Calendar.DAY_OF_YEAR, 3)
                    else -> Triple(SimpleDateFormat("MMM", Locale.getDefault()), Calendar.MONTH, 1)
                }
            }
            currentPeriod == TimePeriod.WEEK -> Triple(SimpleDateFormat("EEE", Locale.getDefault()), Calendar.DAY_OF_YEAR, 1)
            currentPeriod == TimePeriod.MONTH -> Triple(SimpleDateFormat("MMM dd", Locale.getDefault()), Calendar.DAY_OF_YEAR, 2)
            currentPeriod == TimePeriod.YEAR -> Triple(SimpleDateFormat("MMM", Locale.getDefault()), Calendar.MONTH, 1)
            currentPeriod == TimePeriod.CUSTOM -> {
                val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
                when {
                    daysDiff <= 7 -> Triple(SimpleDateFormat("EEE", Locale.getDefault()), Calendar.DAY_OF_YEAR, 1)
                    daysDiff <= 60 -> Triple(SimpleDateFormat("MMM dd", Locale.getDefault()), Calendar.DAY_OF_YEAR, 3)
                    else -> Triple(SimpleDateFormat("MMM", Locale.getDefault()), Calendar.MONTH, 1)
                }
            }
            else -> Triple(SimpleDateFormat("EEE", Locale.getDefault()), Calendar.DAY_OF_YEAR, 1) // Default fallback
        }

        val calendar = Calendar.getInstance()
        calendar.time = startDate
        var index = 0f

        while (calendar.time <= endDate) {
            val currentDate = calendar.time
            val totalForDate = expensesByDate[currentDate]?.sumOf { it.totalAmount } ?: 0.0

            entries.add(Entry(index, totalForDate.toFloat()))
            dateLabels.add(dateFormat.format(currentDate))

            calendar.add(increment, incrementValue)
            index++
        }

        val dataSet = LineDataSet(entries, "Daily Spending").apply {
            color = ContextCompat.getColor(this@AnalyticsActivity, R.color.celadon)
            setCircleColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.asparagus))
            lineWidth = 3f
            circleRadius = 5f
            setCircleHoleColor(Color.WHITE)
            circleHoleRadius = 2f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@AnalyticsActivity, R.drawable.line_chart_gradient)
            valueTextSize = 0f // Hide values on points for cleaner look
            setDrawValues(false)

            // Enable highlighting
            isHighlightEnabled = true
            highlightLineWidth = 2f
            setDrawHighlightIndicators(true)
        }

        lineChart.data = LineData(dataSet)
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)

        // Clear previous limit lines
        lineChart.axisLeft.removeAllLimitLines()

        // Add budget goal lines if available
        currentBudgetGoal?.let { goal ->
            val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
            val dailyMin = goal.minGoalAmount / 30.0 // Assuming monthly budget
            val dailyMax = goal.maxGoalAmount / 30.0

            val minLine = LimitLine(dailyMin.toFloat(), "Min Goal (R${dailyMin.toInt()}/day)").apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.olivine)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 9f
            }

            val maxLine = LimitLine(dailyMax.toFloat(), "Max Goal (R${dailyMax.toInt()}/day)").apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.coral_pink)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 9f
            }

            lineChart.axisLeft.apply {
                addLimitLine(minLine)
                addLimitLine(maxLine)
            }
        }

        lineChart.invalidate()
    }

    private fun setupPieChart() {
        // Group expenses by category
        val expensesByCategory = currentExpenses.groupBy { it.category }
        val totalAmount = currentExpenses.sumOf { it.totalAmount }

        if (totalAmount == 0.0) {
            showNoData()
            return
        }

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Sort categories by amount (descending)
        val sortedCategories = expensesByCategory.entries.sortedByDescending {
            it.value.sumOf { expense -> expense.totalAmount }
        }

        sortedCategories.forEach { (category, expenses) ->
            val categoryTotal = expenses.sumOf { it.totalAmount }
            val percentage = (categoryTotal / totalAmount * 100).toFloat()

            if (percentage >= 0.5f) { // Only show categories with at least 0.5%
                entries.add(PieEntry(categoryTotal.toFloat(), category))
            }
        }

        // Use custom colors
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

        val dataSet = PieDataSet(entries, "").apply {
            setColors(customColors)
            valueTextColor = Color.WHITE
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val percentage = value / totalAmount.toFloat() * 100
                    return if (percentage >= 5f) "${percentage.toInt()}%" else ""
                }
            }
            sliceSpace = 2f
            selectionShift = 8f
        }

        pieChart.data = PieData(dataSet)
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun setupBarChart() {
        // Group expenses by category
        val expensesByCategory = currentExpenses.groupBy { it.category }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val colors = mutableListOf<Int>()

        // Sort by amount and take top categories
        val sortedCategories = expensesByCategory.entries
            .sortedByDescending { it.value.sumOf { expense -> expense.totalAmount } }
            .take(10) // Limit to top 10 categories

        sortedCategories.forEachIndexed { index, (category, expenses) ->
            val total = expenses.sumOf { it.totalAmount }
            entries.add(BarEntry(index.toFloat(), total.toFloat()))
            labels.add(if (category.length > 8) "${category.take(8)}..." else category)

            // Assign colors
            colors.add(when (index % 5) {
                0 -> ContextCompat.getColor(this, R.color.celadon)
                1 -> ContextCompat.getColor(this, R.color.asparagus)
                2 -> ContextCompat.getColor(this, R.color.olivine)
                3 -> ContextCompat.getColor(this, R.color.coral_pink)
                else -> Color.parseColor("#FF9800")
            })
        }

        val dataSet = BarDataSet(entries, "Category Spending").apply {
            setColors(colors)
            valueTextColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_primary)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) "R${value.toInt()}" else ""
                }
            }
        }

        barChart.data = BarData(dataSet)
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Clear previous limit lines
        barChart.axisLeft.removeAllLimitLines()

        // Add budget goal lines if available
        currentBudgetGoal?.let { goal ->
            val minLine = LimitLine(goal.minGoalAmount.toFloat(), "Min Budget").apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.olivine)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
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

            barChart.axisLeft.apply {
                addLimitLine(minLine)
                addLimitLine(maxLine)
            }
        }

        barChart.invalidate()
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("ZAR", "R")
    }
}