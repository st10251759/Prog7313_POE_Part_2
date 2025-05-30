package com.firstproject.prog7313_budgetbuddy.data.models

import java.util.Date

/**
 * Data class representing expense filter criteria
 */
data class ExpenseFilter(
    val searchKeyword: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val selectedCategories: List<String> = emptyList(), // Category IDs
    val sortBy: SortOption = SortOption.DATE_DESC,
    val datePreset: DatePreset = DatePreset.CUSTOM
) {
    fun isEmpty(): Boolean {
        return searchKeyword.isBlank() &&
                startDate == null &&
                endDate == null &&
                minAmount == null &&
                maxAmount == null &&
                selectedCategories.isEmpty()
    }

    fun hasActiveFilters(): Boolean {
        return !isEmpty() || datePreset != DatePreset.CUSTOM
    }
}

/**
 * Enum for sorting options
 */
enum class SortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount"),
    CATEGORY("Category A-Z"),
    DESCRIPTION("Description A-Z")
}

/**
 * Enum for date preset options
 */
enum class DatePreset(val displayName: String, val days: Int) {
    TODAY("Today", 0),
    LAST_7_DAYS("Last 7 Days", 7),
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_3_MONTHS("Last 3 Months", 90),
    LAST_6_MONTHS("Last 6 Months", 180),
    LAST_YEAR("Last Year", 365),
    CUSTOM("Custom Range", -1)
}

/**
 * Extension functions for ExpenseFilter
 */
fun ExpenseFilter.getDateRange(): Pair<Date?, Date?> {
    return when (datePreset) {
        DatePreset.CUSTOM -> Pair(startDate, endDate)
        DatePreset.TODAY -> {
            val today = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time
            val endOfDay = java.util.Calendar.getInstance().apply {
                time = today
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }.time
            Pair(today, endOfDay)
        }
        else -> {
            val endDate = Date()
            val startDate = java.util.Calendar.getInstance().apply {
                time = endDate
                add(java.util.Calendar.DAY_OF_YEAR, -datePreset.days)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time
            Pair(startDate, endDate)
        }
    }
}