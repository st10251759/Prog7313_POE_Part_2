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
 Title: Object declarations and expressions
 Author: Kotlin
 Date Published: 06 November 2024
 Date Accessed: 20 May 2025
 Code Version: v2.1.21
 Availability: https://kotlinlang.org/docs/object-declarations.html#use-anonymous-objects-as-return-and-value-types

 Title: Enum classes
 Author: Kotlin
 Date Published: 25 September 2024
 Date Accessed: 20 May 2025
 Code Version: v2.1.21
 Availability: https://kotlinlang.org/docs/enum-classes.html#
  --------------------------------Code Attribution----------------------------------
*/


//Badge model for achievements with more gamification features
// This is a data model class for an "Achievement Badge" that users can earn in the app.
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val points: Int,  // Points awarded to the user when the badge is earned
    val badgeType: BadgeType,   // Type of badge (e.g., streak, expense count, etc.)
    val requiredStreak: Int = 0, // For streak badges
    val requiredValue: Int = 0   // For other badges
) {
    // Companion object allows us to define static-like functions for the Badge class
    companion object {

        fun getAllBadges(): List<Badge> {
            return listOf(
                // --- STREAK BADGES (based on consecutive daily logging) ---

                // Start with the most basic badges first

                // // A badge for logging your first ever expense. Easy to get and encourages users to start.
                Badge(
                    id = "first_log",
                    name = "First Steps",
                    description = "Log your first expense",
                    points = 10,
                    badgeType = BadgeType.EXPENSE_COUNT,  // Badge is based on number of expenses
                    requiredValue = 1  // Must log 1 expense to earn this badge
                ),

                // --- STREAK BADGES (based on consecutive daily logging) ---

                Badge(
                    id = "week_warrior",
                    name = "Week Warrior",
                    description = "Log expenses for 7 consecutive days",
                    points = 50,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 7 // Must log for 7 days in a row
                ),
                Badge(
                    id = "fortnight_champion",
                    name = "Fortnight Champion",
                    description = "Log expenses for 14 consecutive days",
                    points = 100,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 14     // 14-day streak needed
                ),
                Badge(
                    id = "monthly_master",
                    name = "Monthly Master",
                    description = "Log expenses for 30 consecutive days",
                    points = 200,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 30      // 30-day streak
                ),
                Badge(
                    id = "streak_legend",
                    name = "Streak Legend",
                    description = "Log expenses for 60+ consecutive days",
                    points = 500,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 60     // 60-day streak
                ),

                // --- EXPENSE COUNT BADGES (based on total number of expenses logged) ---
                Badge(
                    id = "expense_rookie",
                    name = "Expense Rookie",
                    description = "Log 10 total expenses",
                    points = 25,
                    badgeType = BadgeType.EXPENSE_COUNT,
                    requiredValue = 10          // Must log 10 expenses total
                ),
                Badge(
                    id = "expense_veteran",
                    name = "Expense Veteran",
                    description = "Log 50 total expenses",
                    points = 75,
                    badgeType = BadgeType.EXPENSE_COUNT,
                    requiredValue = 50          // Must log 50 expenses
                ),
                Badge(
                    id = "expense_master",
                    name = "Expense Master",
                    description = "Log 100 total expenses",
                    points = 150,
                    badgeType = BadgeType.EXPENSE_COUNT,
                    requiredValue = 100         // Must log 100 expenses
                ),

                // --- CATEGORY DIVERSITY BADGES (based on using different expense categories) ---
                Badge(
                    id = "category_explorer",
                    name = "Category Explorer",
                    description = "Use 3 different categories",
                    points = 30,
                    badgeType = BadgeType.CATEGORY_DIVERSITY,
                    requiredValue = 3           // Use at least 3 unique categories
                ),
                Badge(
                    id = "category_master",
                    name = "Category Master",
                    description = "Use 5 different categories",
                    points = 60,
                    badgeType = BadgeType.CATEGORY_DIVERSITY,
                    requiredValue = 5               // Use 5 unique categories
                ),

                // --- TIME-BASED BEHAVIOR BADGES (based on when expenses are logged) ---
                Badge(
                    id = "early_bird",
                    name = "Early Bird",
                    description = "Log 5 expenses before 9 AM",
                    points = 40,
                    badgeType = BadgeType.EARLY_BIRD,
                    requiredValue = 5               // Must log 5 expenses early in the morning
                ),
                Badge(
                    id = "weekend_warrior",
                    name = "Weekend Warrior",
                    description = "Log expenses on 3 weekend days",
                    points = 35,
                    badgeType = BadgeType.WEEKEND_WARRIOR,
                    requiredValue = 3                       // Must log expenses on 3 separate weekends
                ),

                // --- BUDGETING BEHAVIOR BADGES (based on staying under a budget) ---
                Badge(
                    id = "budget_keeper",
                    name = "Budget Keeper",
                    description = "Stay under budget for 5 days",
                    points = 80,
                    badgeType = BadgeType.BUDGET_KEEPER,
                    requiredValue = 5                       // Be under budget for 5 days
                ),
                Badge(
                    id = "frugal_master",
                    name = "Frugal Master",
                    description = "Stay under budget for 15 days",
                    points = 150,
                    badgeType = BadgeType.BUDGET_KEEPER,
                    requiredValue = 15                       // Be under budget for 15 days
                )
            )
        }
    }
}

// This enum defines all the types of badges available in the app.
// Each badge type represents a different way of tracking user achievements.
enum class BadgeType {
    STREAK,           // Based on consecutive days
    EXPENSE_COUNT,    // Based on total expenses logged
    BUDGET_KEEPER,    // Based on staying under budget
    CATEGORY_DIVERSITY, // Based on using different categories
    EARLY_BIRD,       // Based on logging before 9 AM
    WEEKEND_WARRIOR   // Based on logging on weekends
}