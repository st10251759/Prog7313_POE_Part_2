package com.firstproject.prog7313_budgetbuddy.data.models

// Enhanced Badge model for achievements with more gamification features
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val points: Int,
    val badgeType: BadgeType,
    val requiredStreak: Int = 0, // For streak badges
    val requiredValue: Int = 0   // For other badges
) {
    companion object {
        fun getAllBadges(): List<Badge> {
            return listOf(
                // **REORDERED**: Start with the most basic badges first

                // First achievement - very easy to get
                Badge(
                    id = "first_log",
                    name = "First Steps",
                    description = "Log your first expense",
                    points = 10,
                    badgeType = BadgeType.EXPENSE_COUNT,  // **FIXED**: Should be expense count, not streak
                    requiredValue = 1  // **FIXED**: Use requiredValue for expense count
                ),

                // Streak badges in order of difficulty
                Badge(
                    id = "week_warrior",
                    name = "Week Warrior",
                    description = "Log expenses for 7 consecutive days",
                    points = 50,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 7
                ),
                Badge(
                    id = "fortnight_champion",
                    name = "Fortnight Champion",
                    description = "Log expenses for 14 consecutive days",
                    points = 100,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 14
                ),
                Badge(
                    id = "monthly_master",
                    name = "Monthly Master",
                    description = "Log expenses for 30 consecutive days",
                    points = 200,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 30
                ),
                Badge(
                    id = "streak_legend",
                    name = "Streak Legend",
                    description = "Log expenses for 60+ consecutive days",
                    points = 500,
                    badgeType = BadgeType.STREAK,
                    requiredStreak = 60
                ),

                // Expense count badges - ordered by difficulty
                Badge(
                    id = "expense_rookie",
                    name = "Expense Rookie",
                    description = "Log 10 total expenses",
                    points = 25,
                    badgeType = BadgeType.EXPENSE_COUNT,
                    requiredValue = 10
                ),
                Badge(
                    id = "expense_veteran",
                    name = "Expense Veteran",
                    description = "Log 50 total expenses",
                    points = 75,
                    badgeType = BadgeType.EXPENSE_COUNT,
                    requiredValue = 50
                ),
                Badge(
                    id = "expense_master",
                    name = "Expense Master",
                    description = "Log 100 total expenses",
                    points = 150,
                    badgeType = BadgeType.EXPENSE_COUNT,
                    requiredValue = 100
                ),

                // Category badges
                Badge(
                    id = "category_explorer",
                    name = "Category Explorer",
                    description = "Use 3 different categories",  // **REDUCED**: More achievable
                    points = 30,
                    badgeType = BadgeType.CATEGORY_DIVERSITY,
                    requiredValue = 3
                ),
                Badge(
                    id = "category_master",
                    name = "Category Master",
                    description = "Use 5 different categories",  // **REDUCED**: More realistic
                    points = 60,
                    badgeType = BadgeType.CATEGORY_DIVERSITY,
                    requiredValue = 5
                ),

                // Special behavior badges - easier to achieve
                Badge(
                    id = "early_bird",
                    name = "Early Bird",
                    description = "Log 5 expenses before 9 AM",  // **REDUCED**: More achievable
                    points = 40,
                    badgeType = BadgeType.EARLY_BIRD,
                    requiredValue = 5
                ),
                Badge(
                    id = "weekend_warrior",
                    name = "Weekend Warrior",
                    description = "Log expenses on 3 weekend days",  // **REDUCED**: More achievable
                    points = 35,
                    badgeType = BadgeType.WEEKEND_WARRIOR,
                    requiredValue = 3
                ),

                // Budget badges
                Badge(
                    id = "budget_keeper",
                    name = "Budget Keeper",
                    description = "Stay under budget for 5 days",  // **REDUCED**: More achievable
                    points = 80,
                    badgeType = BadgeType.BUDGET_KEEPER,
                    requiredValue = 5
                ),
                Badge(
                    id = "frugal_master",
                    name = "Frugal Master",
                    description = "Stay under budget for 15 days",  // **REDUCED**: More realistic
                    points = 150,
                    badgeType = BadgeType.BUDGET_KEEPER,
                    requiredValue = 15
                )
            )
        }
    }
}

// NEW: Badge types for different achievement categories
enum class BadgeType {
    STREAK,           // Based on consecutive days
    EXPENSE_COUNT,    // Based on total expenses logged
    BUDGET_KEEPER,    // Based on staying under budget
    CATEGORY_DIVERSITY, // Based on using different categories
    EARLY_BIRD,       // Based on logging before 9 AM
    WEEKEND_WARRIOR   // Based on logging on weekends
}