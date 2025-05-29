package com.firstproject.prog7313_budgetbuddy.data.models

// Badge model for achievements
data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconResId: Int = 0, // Resource ID for badge icon
    val requiredStreak: Int = 0, // Days required to earn this badge
    val points: Int = 0 // Points awarded when earned
) {
    companion object {
        // Predefined badges
        val FIRST_LOG = Badge(
            id = "first_log",
            name = "First Step",
            description = "Logged your first expense",
            iconResId = 0, // Will be set in the activity
            requiredStreak = 1,
            points = 10
        )

        val WEEK_WARRIOR = Badge(
            id = "week_warrior",
            name = "Week Warrior",
            description = "7 day streak achieved!",
            iconResId = 0,
            requiredStreak = 7,
            points = 50
        )

        val FORTNIGHT_CHAMPION = Badge(
            id = "fortnight_champion",
            name = "Fortnight Champion",
            description = "14 day streak achieved!",
            iconResId = 0,
            requiredStreak = 14,
            points = 100
        )

        val MONTHLY_MASTER = Badge(
            id = "monthly_master",
            name = "Monthly Master",
            description = "30 day streak achieved!",
            iconResId = 0,
            requiredStreak = 30,
            points = 200
        )

        val STREAK_LEGEND = Badge(
            id = "streak_legend",
            name = "Streak Legend",
            description = "60 day streak achieved!",
            iconResId = 0,
            requiredStreak = 60,
            points = 500
        )

        fun getAllBadges(): List<Badge> {
            return listOf(FIRST_LOG, WEEK_WARRIOR, FORTNIGHT_CHAMPION, MONTHLY_MASTER, STREAK_LEGEND)
        }
    }
}