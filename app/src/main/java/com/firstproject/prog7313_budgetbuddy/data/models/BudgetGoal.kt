package com.firstproject.prog7313_budgetbuddy.data.models

import com.google.firebase.Timestamp
import java.util.Date

// BudgetGoal model for Firestore
data class BudgetGoal(
    var id: String = "",
    val userId: String = "",
    val minGoalAmount: Double = 0.0,
    val maxGoalAmount: Double = 0.0,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "minGoalAmount" to minGoalAmount,
            "maxGoalAmount" to maxGoalAmount,
            "startDate" to startDate,
            "endDate" to endDate
        )
    }


    // Helper methods to convert Timestamp to Date for compatibility
    fun getStartDateAsDate(): Date {
        return startDate.toDate()
    }

    fun getEndDateAsDate(): Date {
        return endDate.toDate()
    }
}
