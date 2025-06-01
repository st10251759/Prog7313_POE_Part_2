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

// Data class representing a user's gamification-related statistics and achievements
data class GamificationStats(
    val currentStreak: Int = 0,          // Current number of consecutive active days
    val longestStreak: Int = 0,          // Longest streak of consecutive active days
    val totalPoints: Int = 0,            // Total gamification points earned by the user
    val totalExpenses: Int = 0,          // Total number of expenses recorded
    val categoriesUsed: Int = 0,         // Number of unique categories the user has used
    val earlyBirdCount: Int = 0,         // Number of expenses recorded early in the day (e.g., before 9 AM)
    val weekendCount: Int = 0,           // Number of expenses recorded on weekends
    val budgetKeeperDays: Int = 0,       // Days where user stayed within budget
    val perfectWeeks: Int = 0,           // Number of weeks where user met all goals (e.g., stayed on budget, recorded daily)
    val streakLevel: String = "Newcomer",// User's level based on current streak (e.g., "Beginner", "Expert")
    val progressToNext: Float = 0f,      // Progress (0.0 to 1.0) towards next milestone or streak level
    val nextMilestone: Int = 7,          // Next goal/streak number the user is aiming to reach
    val badges: List<String> = emptyList(),      // List of badge identifiers earned by the user
    val achievements: List<String> = emptyList() // List of named achievements unlocked
)
