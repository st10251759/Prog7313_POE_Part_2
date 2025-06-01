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

 Title: Enum classes
 Author: Kotlin
 Date Published: 25 September 2024
 Date Accessed: 20 May 2025
 Code Version: v2.1.21
 Availability: https://kotlinlang.org/docs/enum-classes.html#
  --------------------------------Code Attribution----------------------------------
*/

import java.util.Date

// A filter model used to search, filter, and sort a list of expenses based on various criteria
data class ExpenseFilter(
    val searchKeyword: String = "",             // Keyword to match expense descriptions
    val startDate: Date? = null,                // Optional: Start date for filtering (e.g. from this date onward)
    val endDate: Date? = null,                  // Optional: End date for filtering (e.g. up to this date)
    val minAmount: Double? = null,              // Optional: Minimum amount to filter expenses
    val maxAmount: Double? = null,              // Optional: Maximum amount to filter expenses
    val selectedCategories: List<String> = emptyList(), // List of selected category IDs to include in the filter
    val sortBy: SortOption = SortOption.DATE_DESC,      // Sorting option (e.g. newest first, by amount, etc.)
    val datePreset: DatePreset = DatePreset.CUSTOM      // Preset date range (e.g. "This Month", "Last 7 Days")
) {
    // Returns true if all filter fields are empty or unset
    fun isEmpty(): Boolean {
        return searchKeyword.isBlank() &&       // No search text
                startDate == null &&            // No start date
                endDate == null &&              // No end date
                minAmount == null &&            // No min amount
                maxAmount == null &&            // No max amount
                selectedCategories.isEmpty()    // No categories selected
    }

    // Returns true if any filters are active, including if a preset (not CUSTOM) is selected
    fun hasActiveFilters(): Boolean {
        return !isEmpty() || datePreset != DatePreset.CUSTOM
    }
}

// Enum class that defines the available sorting options for displaying expenses
enum class SortOption(val displayName: String) {

    DATE_DESC("Newest First"),         // Sort by date, most recent expenses shown first
    DATE_ASC("Oldest First"),          // Sort by date, oldest expenses shown first
    AMOUNT_DESC("Highest Amount"),     // Sort by amount, from highest to lowest
    AMOUNT_ASC("Lowest Amount"),       // Sort by amount, from lowest to highest
    CATEGORY("Category A-Z"),          // Sort alphabetically by category name
    DESCRIPTION("Description A-Z")     // Sort alphabetically by description
}

// Enum class that defines preset date ranges for filtering expenses
enum class DatePreset(val displayName: String, val days: Int) {

    TODAY("Today", 0),                      // Filter for expenses from the current day only
    LAST_7_DAYS("Last 7 Days", 7),          // Filter for expenses from the past 7 days
    LAST_30_DAYS("Last 30 Days", 30),       // Filter for expenses from the past 30 days
    LAST_3_MONTHS("Last 3 Months", 90),     // Filter for expenses from the past 3 months (~90 days)
    LAST_6_MONTHS("Last 6 Months", 180),    // Filter for expenses from the past 6 months (~180 days)
    LAST_YEAR("Last Year", 365),            // Filter for expenses from the past year
    CUSTOM("Custom Range", -1)              // User-defined custom date range (not fixed to a specific number of days)
}

// Extension function for ExpenseFilter that returns a date range (start and end dates)
// based on the selected DatePreset
fun ExpenseFilter.getDateRange(): Pair<Date?, Date?> {
    return when (datePreset) {

        // If CUSTOM preset is selected, use the manually provided start and end dates
        DatePreset.CUSTOM -> Pair(startDate, endDate)

        // If TODAY is selected, return the start and end of the current day
        DatePreset.TODAY -> {
            val today = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)        // Set hour to 00:00
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time

            val endOfDay = java.util.Calendar.getInstance().apply {
                time = today
                set(java.util.Calendar.HOUR_OF_DAY, 23)       // Set time to 23:59:59.999
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }.time

            Pair(today, endOfDay) // Return today's full time range
        }

        // For all other presets (e.g., LAST_7_DAYS, LAST_30_DAYS, etc.)
        else -> {
            val endDate = Date() // Current date and time
            val startDate = java.util.Calendar.getInstance().apply {
                time = endDate
                add(java.util.Calendar.DAY_OF_YEAR, -datePreset.days) // Subtract days based on preset
                set(java.util.Calendar.HOUR_OF_DAY, 0)               // Start of that day
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time

            Pair(startDate, endDate) // Return computed date range
        }
    }
}
