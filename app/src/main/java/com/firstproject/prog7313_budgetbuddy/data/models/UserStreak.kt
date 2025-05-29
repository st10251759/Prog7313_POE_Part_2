package com.firstproject.prog7313_budgetbuddy.data.models

import com.google.firebase.Timestamp
import java.util.Date

// UserStreak model for tracking daily expense logging
data class UserStreak(
    var id: String = "",
    val userId: String = "",
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var lastLogDate: Timestamp = Timestamp.now(),
    val totalDaysLogged: Int = 0,
    val badges: List<String> = emptyList(), // List of earned badge IDs
    val points: Int = 0
) {
    // Empty constructor required for Firestore
    constructor() : this("", "", 0, 0, Timestamp.now(), 0, emptyList(), 0)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "currentStreak" to currentStreak,
            "longestStreak" to longestStreak,
            "lastLogDate" to lastLogDate,
            "totalDaysLogged" to totalDaysLogged,
            "badges" to badges,
            "points" to points
        )
    }

    // Helper method to get last log date as Date
    fun getLastLogDateAsDate(): Date {
        return lastLogDate.toDate()
    }
}