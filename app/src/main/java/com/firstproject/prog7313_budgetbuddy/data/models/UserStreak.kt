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

import com.google.firebase.Timestamp
import java.util.*

// Data class representing a user's activity streak and engagement stats
data class UserStreak(
    var id: String = "",                            // Unique ID for the streak record (e.g., Firestore document ID)
    val userId: String = "",                        // ID of the user this streak belongs to
    val currentStreak: Int = 0,                     // Current number of consecutive logging days
    val longestStreak: Int = 0,                     // Highest streak achieved by the user
    val lastLogDate: Timestamp = Timestamp(Date(0)),// Timestamp of the user's most recent logged activity
    val totalDaysLogged: Int = 0,                   // Total number of days the user has logged expenses
    val points: Int = 0,                            // Total points accumulated for gamification
    val badges: List<String> = emptyList(),         // List of badge IDs earned by the user
    val totalExpensesLogged: Int = 0,               // Total number of expenses the user has logged
    val categoriesUsed: List<String> = emptyList(), // List of unique category IDs used by the user
    val earlyBirdCount: Int = 0,                    // Number of times the user logged expenses early in the day
    val weekendLogCount: Int = 0,                   // Number of logs made on weekends
    val budgetKeeperDays: Int = 0,                  // Days the user remained within their budget
    val perfectWeeks: Int = 0,                      // Weeks where the user met all activity or financial goals
    val lastStreakBreak: Timestamp? = null,         // Timestamp when the user's last streak was broken
    val achievements: List<String> = emptyList()    // List of unlocked achievements
) {

    // Converts Firestore Timestamp to Java Date object
    fun getLastLogDateAsDate(): Date {
        return lastLogDate.toDate()
    }

    // Returns the user's current streak level as a descriptive label
    fun getStreakLevel(): String {
        return when (currentStreak) {
            0 -> "Newcomer"
            in 1..6 -> "Beginner"
            in 7..13 -> "Week Warrior"
            in 14..29 -> "Fortnight Champion"
            in 30..59 -> "Monthly Master"
            else -> "Streak Legend"
        }
    }

    // Calculates progress (0.0 to 1.0) toward the next milestone level
    fun getProgressToNextMilestone(): Float {
        val nextMilestone = getNextStreakMilestone()
        return if (nextMilestone > 0) {
            currentStreak.toFloat() / nextMilestone
        } else {
            1.0f // Max level achieved
        }
    }

    // Returns the next milestone streak value for gamification
    fun getNextStreakMilestone(): Int {
        return when {
            currentStreak < 7 -> 7
            currentStreak < 14 -> 14
            currentStreak < 30 -> 30
            currentStreak < 60 -> 60
            else -> 0 // No more milestones to reach
        }
    }

    // Converts the data class to a Firestore-compatible map for saving
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "currentStreak" to currentStreak,
            "longestStreak" to longestStreak,
            "lastLogDate" to lastLogDate,
            "totalDaysLogged" to totalDaysLogged,
            "points" to points,
            "badges" to badges,
            "totalExpensesLogged" to totalExpensesLogged,
            "categoriesUsed" to categoriesUsed,
            "earlyBirdCount" to earlyBirdCount,
            "weekendLogCount" to weekendLogCount,
            "budgetKeeperDays" to budgetKeeperDays,
            "perfectWeeks" to perfectWeeks,
            "lastStreakBreak" to (lastStreakBreak ?: Timestamp.now()),
            "achievements" to achievements
        )
    }
}
