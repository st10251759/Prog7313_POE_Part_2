package com.firstproject.prog7313_budgetbuddy.data.models

data class GamificationStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalPoints: Int = 0,
    val totalExpenses: Int = 0,
    val categoriesUsed: Int = 0,
    val earlyBirdCount: Int = 0,
    val weekendCount: Int = 0,
    val budgetKeeperDays: Int = 0,
    val perfectWeeks: Int = 0,
    val streakLevel: String = "Newcomer",
    val progressToNext: Float = 0f,
    val nextMilestone: Int = 7,
    val badges: List<String> = emptyList(),
    val achievements: List<String> = emptyList()
)