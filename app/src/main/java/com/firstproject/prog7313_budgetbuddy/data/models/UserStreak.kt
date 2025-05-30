package com.firstproject.prog7313_budgetbuddy.data.models

import com.google.firebase.Timestamp
import java.util.*

data class UserStreak(
    var id: String = "",
    val userId: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLogDate: Timestamp = Timestamp(Date(0)),
    val totalDaysLogged: Int = 0,
    val points: Int = 0,
    val badges: List<String> = emptyList(),
    val totalExpensesLogged: Int = 0,
    val categoriesUsed: List<String> = emptyList(),
    val earlyBirdCount: Int = 0,
    val weekendLogCount: Int = 0,
    val budgetKeeperDays: Int = 0,
    val perfectWeeks: Int = 0,
    val lastStreakBreak: Timestamp? = null,
    val achievements: List<String> = emptyList()
) {

    fun getLastLogDateAsDate(): Date {
        return lastLogDate.toDate()
    }

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

    fun getProgressToNextMilestone(): Float {
        val nextMilestone = getNextStreakMilestone()
        return if (nextMilestone > 0) {
            currentStreak.toFloat() / nextMilestone
        } else {
            1.0f // Already at max
        }
    }

    fun getNextStreakMilestone(): Int {
        return when {
            currentStreak < 7 -> 7
            currentStreak < 14 -> 14
            currentStreak < 30 -> 30
            currentStreak < 60 -> 60
            else -> 0 // Already at max
        }
    }

    // Convert to Map for Firestore
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