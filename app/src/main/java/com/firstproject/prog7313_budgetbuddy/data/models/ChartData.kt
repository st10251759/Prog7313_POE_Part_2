package com.firstproject.prog7313_budgetbuddy.data.models

import java.util.Date

// Enhanced data model for chart entries
data class ChartData(
    val date: Date,
    val amount: Double,
    val category: String? = null,
    val description: String? = null
)

// Time period enum for analytics with more options
enum class TimePeriod(val displayName: String, val days: Int) {
    WEEK("Last 7 Days", 7),
    MONTH("Last 30 Days", 30),
    YEAR("Last 365 Days", 365),
    CUSTOM("Custom Range", 0)
}

// Chart type enum with descriptions
enum class ChartType(val displayName: String, val description: String) {
    LINE("Line Chart", "View spending trends over time"),
    PIE("Pie Chart", "See category breakdown"),
    BAR("Bar Chart", "Compare category spending")
}

// Aggregated spending data for analytics
data class SpendingAnalytics(
    val totalSpent: Double,
    val dailyAverage: Double,
    val categoryBreakdown: List<CategoryWithAmount>,
    val dailySpending: List<ChartData>,
    val weeklySpending: List<ChartData>,
    val monthlySpending: List<ChartData>,
    val topCategories: List<CategoryWithAmount>,
    val spendingTrend: SpendingTrend,
    val budgetStatus: BudgetStatus
)

// Spending trend analysis
data class SpendingTrend(
    val direction: TrendDirection,
    val percentage: Float,
    val description: String
)

enum class TrendDirection {
    INCREASING, DECREASING, STABLE
}

// Budget status
data class BudgetStatus(
    val status: Status,
    val percentage: Float,
    val amountLeft: Double,
    val daysLeft: Int
)

enum class Status {
    UNDER_BUDGET, ON_TRACK, OVER_BUDGET, NO_BUDGET
}

// Enhanced category with amount data
data class CategoryAnalytics(
    val category: CategoryWithAmount,
    val trend: SpendingTrend,
    val averagePerTransaction: Double,
    val transactionCount: Int,
    val lastExpenseDate: Date?
)