package com.firstproject.prog7313_budgetbuddy.data.models

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
 Title: Data classes
 Author: Kotlin
 Date Published: 06 November 2024
 Date Accessed: 21 May 2025
 Code Version: v2.1.21
 Availability: https://kotlinlang.org/docs/data-classes.html
  --------------------------------Code Attribution----------------------------------
*/

//Imports

import java.util.Date

// Enhanced data model for chart entries
data class ChartData(
    val date: Date,                 // The date of the expense or data point
    val amount: Double,             // The amount spent on that day
    val category: String? = null,   // Optional: The category this amount belongs to (e.g., "Food")
    val description: String? = null // Optional: A short description of the expense
)

// Time period enum for analytics with more options
enum class TimePeriod(val displayName: String, val days: Int) {
    WEEK("Last 7 Days", 7),     // 7-day summary
    MONTH("Last 30 Days", 30),  // 30-day summary
    YEAR("Last 365 Days", 365), // 1-year summary
    CUSTOM("Custom Range", 0)   // Custom date range selected by the user
}

// Enum class to define different types of charts for visualizing data
enum class ChartType(val displayName: String, val description: String) {
    LINE("Line Chart", "View spending trends over time"),   // Shows changes over time
    PIE("Pie Chart", "See category breakdown"),             // Shows percentage per category
    BAR("Bar Chart", "Compare category spending")           // Compares amounts per category
}

// This data class aggregates various analytics metrics about spending
data class SpendingAnalytics(
    val totalSpent: Double,      // Total amount spent in the selected time period
    val dailyAverage: Double,    // Average amount spent per day
    val categoryBreakdown: List<CategoryWithAmount>,    // List of spending per category
    val dailySpending: List<ChartData>,             // List of spending per day for a line chart
    val weeklySpending: List<ChartData>,            // Aggregated weekly spending
    val monthlySpending: List<ChartData>,           // Aggregated monthly spending
    val topCategories: List<CategoryWithAmount>,    // Categories with the most spending
    val spendingTrend: SpendingTrend,                // Trend analysis (increasing/decreasing)
    val budgetStatus: BudgetStatus                   // Budget usage status
)

// Represents the trend in user spending behavior
data class SpendingTrend(
    val direction: TrendDirection,      // Direction of the trend: up/down/stable
    val percentage: Float,              // Percentage change in spending
    val description: String             // Human-readable explanation (e.g., "You spent 20% more this month")
)

// Enum for identifying whether spending is going up, down, or staying the same
enum class TrendDirection {
    INCREASING,     // Spending is increasing
    DECREASING,     // Spending is decreasing
    STABLE          // Spending is consistent
}

// Represents the user's current budget status
data class BudgetStatus(
    val status: Status,                        // Current budget condition (under/over/on track)
    val percentage: Float,                     // Percentage of the budget that has been used
    val amountLeft: Double,                    // Amount of money left in the budget
    val daysLeft: Int                          // Days remaining in the budget period
)

// Enum to represent different budget statuses
enum class Status {
    UNDER_BUDGET,      // Spending is below the budget limit
    ON_TRACK,          // Spending is within expected range
    OVER_BUDGET,       // Spending has exceeded the budget
    NO_BUDGET          // No budget has been set
}

// Analytics for a specific category (e.g., food, transport)
data class CategoryAnalytics(
    val category: CategoryWithAmount,          // The category name and total spent
    val trend: SpendingTrend,                  // Trend within this specific category
    val averagePerTransaction: Double,         // Average spend per transaction
    val transactionCount: Int,                 // Number of transactions in this category
    val lastExpenseDate: Date?                 // Date of the most recent expense in this category
)