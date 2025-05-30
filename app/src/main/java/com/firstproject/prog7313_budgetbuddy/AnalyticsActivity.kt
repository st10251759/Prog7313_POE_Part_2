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

                // Format Y-axis values as currency with proper intervals
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000 -> "R${(value / 1000).toInt()}k"
                            else -> "R${value.toInt()}"
                        }
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
            legend.apply {
                isEnabled = true
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
            }
            setFitBars(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
                textSize = 10f
                labelRotationAngle = 0f
                setAvoidFirstLastClipping(true)
                // Don't show labels on X-axis for bar chart
                setDrawLabels(false)
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
                        return when {
                            value >= 1000 -> "R${(value / 1000).toInt()}k"
                            else -> "R${value.toInt()}"
                        }
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
                    TimePeriod.CUSTOM -> goal.maxGoalAmount
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
                tvChartTitle.text = "📈 Cumulative Spending Trend"
                setupImprovedLineChart()
                chartLegend.visibility = if (currentBudgetGoal != null) View.VISIBLE else View.GONE
            }
            ChartType.PIE -> {
                pieChart.visibility = View.VISIBLE
                tvChartTitle.text = "🥧 Category Breakdown"
                setupImprovedPieChart()
                chartLegend.visibility = View.GONE
            }
            ChartType.BAR -> {
                barChart.visibility = View.VISIBLE
                tvChartTitle.text = "📊 Category Comparison"
                setupImprovedBarChart()
                chartLegend.visibility = View.GONE
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

    private fun setupImprovedLineChart() {
        val entries = mutableListOf<Entry>()
        val (startDate, endDate) = getDateRange()

        Log.d(TAG, "Setting up line chart with ${currentExpenses.size} expenses")

        // Determine grouping and labels based on period
        val (groupedData, labels) = when {
            isCustomDateRange -> {
                val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
                when {
                    daysDiff <= 7 -> groupByDays(startDate, endDate)
                    daysDiff <= 60 -> groupByWeeks(startDate, endDate)
                    else -> groupByMonths(startDate, endDate)
                }
            }
            currentPeriod == TimePeriod.WEEK -> groupByDays(startDate, endDate)
            currentPeriod == TimePeriod.MONTH -> groupByWeeks(startDate, endDate)
            currentPeriod == TimePeriod.YEAR -> groupByQuarters(startDate, endDate)
            else -> groupByDays(startDate, endDate)
        }

        Log.d(TAG, "Grouped data: ${groupedData.size} periods, Labels: $labels")

        // Create cumulative entries - Fixed logic
        var cumulativeTotal = 0.0
        groupedData.forEachIndexed { index, amount ->
            cumulativeTotal += amount
            entries.add(Entry(index.toFloat(), cumulativeTotal.toFloat()))
            Log.d(TAG, "Entry $index: Amount = $amount, Cumulative = $cumulativeTotal")
        }

        // Ensure we have data to plot
        if (entries.isEmpty()) {
            Log.w(TAG, "No entries to plot")
            showNoData()
            return
        }

        val dataSet = LineDataSet(entries, "Cumulative Spending").apply {
            color = ContextCompat.getColor(this@AnalyticsActivity, R.color.celadon)
            setCircleColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.asparagus))
            lineWidth = 3f
            circleRadius = 6f
            setCircleHoleColor(Color.WHITE)
            circleHoleRadius = 3f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@AnalyticsActivity, R.drawable.line_chart_gradient)
            valueTextSize = 0f
            setDrawValues(false)
            isHighlightEnabled = true
            highlightLineWidth = 2f
            setDrawHighlightIndicators(true)
            mode = LineDataSet.Mode.LINEAR
        }

        lineChart.data = LineData(dataSet)
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Fix X-axis configuration
        lineChart.xAxis.apply {
            granularity = 1f
            setLabelCount(labels.size, false)
            axisMinimum = 0f
            axisMaximum = (labels.size - 1).toFloat()
        }

        // Set appropriate Y-axis scaling - Fixed calculation
        val maxValue = if (cumulativeTotal > 0) cumulativeTotal.toFloat() else 1000f
        val yAxisMax = when {
            maxValue <= 500 -> 500f
            maxValue <= 1000 -> 1000f
            maxValue <= 2000 -> 2000f
            maxValue <= 5000 -> 5000f
            maxValue <= 10000 -> 10000f
            else -> ((maxValue / 5000).toInt() + 1) * 5000f
        }

        lineChart.axisLeft.apply {
            axisMaximum = yAxisMax
            axisMinimum = 0f
            setLabelCount(6, false)
        }

        // Clear previous limit lines and add budget goal lines if available
        lineChart.axisLeft.removeAllLimitLines()
        currentBudgetGoal?.let { goal ->
            // For cumulative spending chart, we want to show the TOTAL budget goals
            // not scaled down by time period, since we're showing cumulative amounts

            Log.d(TAG, "Budget Goal - Min: ${goal.minGoalAmount}, Max: ${goal.maxGoalAmount}")

            // Use the full monthly budget amounts for reference lines
            val minGoalForPeriod = goal.minGoalAmount.toFloat()
            val maxGoalForPeriod = goal.maxGoalAmount.toFloat()

            Log.d(TAG, "Plotting goal lines at Min: $minGoalForPeriod, Max: $maxGoalForPeriod")

            val minLine = LimitLine(minGoalForPeriod, "Min Goal (${formatCurrency(goal.minGoalAmount)})").apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.olivine)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 9f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.olivine)
            }

            val maxLine = LimitLine(maxGoalForPeriod, "Max Goal (${formatCurrency(goal.maxGoalAmount)})").apply {
                lineColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.coral_pink)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 9f
                textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.coral_pink)
            }

            lineChart.axisLeft.apply {
                addLimitLine(minLine)
                addLimitLine(maxLine)

                // Ensure Y-axis maximum accommodates the goal lines
                axisMaximum = maxOf(yAxisMax, maxGoalForPeriod + 500f)
            }
        }

        Log.d(TAG, "Line chart setup complete with ${entries.size} entries")
        lineChart.invalidate()
    }

    private fun groupByDays(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>()
        val labels = mutableListOf<String>()
        val expensesByDate = mutableMapOf<String, Double>()

        // First, group expenses by date string
        currentExpenses.forEach { expense ->
            val calendar = Calendar.getInstance()
            calendar.time = expense.getExpenseDateAsDate()
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            expensesByDate[dateKey] = expensesByDate.getOrDefault(dateKey, 0.0) + expense.totalAmount
        }

        val calendar = Calendar.getInstance()
        calendar.time = startDate
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        while (calendar.time <= endDate) {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val totalForDate = expensesByDate[dateKey] ?: 0.0
            amounts.add(totalForDate)
            labels.add(dayFormat.format(calendar.time))

            Log.d(TAG, "Day ${dayFormat.format(calendar.time)}: R$totalForDate")
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        Log.d(TAG, "Days grouping: ${amounts.size} days, total expenses: ${amounts.sum()}")
        return Pair(amounts, labels)
    }

    private fun groupByWeeks(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>()
        val labels = mutableListOf<String>()

        val calendar = Calendar.getInstance()
        calendar.time = startDate
        // Start from the beginning of the week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        var weekNumber = 1

        while (calendar.time <= endDate) {
            val weekStart = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val weekEnd = if (calendar.time > endDate) endDate else calendar.time

            val weekTotal = currentExpenses.filter { expense ->
                val expenseDate = expense.getExpenseDateAsDate()
                expenseDate >= weekStart && expenseDate <= weekEnd
            }.sumOf { it.totalAmount }

            amounts.add(weekTotal)
            labels.add("Week $weekNumber")

            Log.d(TAG, "Week $weekNumber: R$weekTotal")

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            weekNumber++
        }

        Log.d(TAG, "Weeks grouping: ${amounts.size} weeks, total expenses: ${amounts.sum()}")
        return Pair(amounts, labels)
    }

    private fun groupByQuarters(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>()
        val labels = mutableListOf<String>()

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        for (quarter in 1..4) {
            val quarterStart = Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, (quarter - 1) * 3)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val quarterEnd = Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, quarter * 3 - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val quarterTotal = currentExpenses.filter { expense ->
                val expenseDate = expense.getExpenseDateAsDate()
                expenseDate >= quarterStart.time && expenseDate <= quarterEnd.time
            }.sumOf { it.totalAmount }

            amounts.add(quarterTotal)
            labels.add("Q$quarter")
        }

        return Pair(amounts, labels)
    }

    private fun groupByMonths(startDate: Date, endDate: Date): Pair<List<Double>, List<String>> {
        val amounts = mutableListOf<Double>()
        val labels = mutableListOf<String>()

        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        while (calendar.time <= endDate) {
            val monthStart = calendar.time
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val monthEnd = if (calendar.time > endDate) endDate else calendar.time

            val monthTotal = currentExpenses.filter { expense ->
                val expenseDate = expense.getExpenseDateAsDate()
                expenseDate >= monthStart && expenseDate <= monthEnd
            }.sumOf { it.totalAmount }

            amounts.add(monthTotal)
            labels.add(monthFormat.format(monthStart))

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return Pair(amounts, labels)
    }

    private fun setupImprovedPieChart() {
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

        // Move legend below the chart
        pieChart.legend.apply {
            isEnabled = true
            orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
            textSize = 10f
            isWordWrapEnabled = true

            // Custom legend labels with percentages
            val legendLabels = mutableListOf<String>()
            entries.forEach { entry ->
                val percentage = entry.value / totalAmount.toFloat() * 100
                legendLabels.add("${entry.label} (${percentage.toInt()}%)")
            }
            setCustom(legendLabels.mapIndexed { index, label ->
                com.github.mikephil.charting.components.LegendEntry().apply {
                    this.label = label
                    formColor = customColors[index % customColors.size]
                }
            })
        }

        pieChart.data = PieData(dataSet)
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun setupImprovedBarChart() {
        // Group expenses by category
        val expensesByCategory = currentExpenses.groupBy { it.category }

        val entries = mutableListOf<BarEntry>()
        val categoryLabels = mutableListOf<String>()
        val colors = mutableListOf<Int>()

        // Sort by amount and take top categories
        val sortedCategories = expensesByCategory.entries
            .sortedByDescending { it.value.sumOf { expense -> expense.totalAmount } }
            .take(10) // Limit to top 10 categories

        // Custom colors for consistency
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

        sortedCategories.forEachIndexed { index, (category, expenses) ->
            val total = expenses.sumOf { it.totalAmount }
            entries.add(BarEntry(index.toFloat(), total.toFloat()))
            categoryLabels.add(category)
            colors.add(customColors[index % customColors.size])
        }

        val dataSet = BarDataSet(entries, "Category Spending").apply {
            setColors(colors)
            valueTextColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_primary)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) {
                        when {
                            value >= 1000 -> "R${(value / 1000).toInt()}k"
                            else -> "R${value.toInt()}"
                        }
                    } else ""
                }
            }
        }

        // Set up legend with category names and colors
        barChart.legend.apply {
            isEnabled = true
            orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
            verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.CENTER
            horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
            textColor = ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary)
            textSize = 10f

            // Custom legend entries
            setCustom(categoryLabels.mapIndexed { index, label ->
                com.github.mikephil.charting.components.LegendEntry().apply {
                    this.label = if (label.length > 12) "${label.take(12)}..." else label
                    formColor = colors[index]
                }
            })
        }

        barChart.data = BarData(dataSet)

        // Don't show X-axis labels since we're using legend
        barChart.xAxis.setDrawLabels(false)

        // Clear previous limit lines and add budget goal lines if available
        barChart.axisLeft.removeAllLimitLines()
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