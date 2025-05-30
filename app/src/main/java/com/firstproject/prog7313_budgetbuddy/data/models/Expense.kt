package com.firstproject.prog7313_budgetbuddy.data.models

import com.google.firebase.Timestamp
import java.util.Date

// Expense model for Firestore
data class Expense(
    var id: String = "",
    val userId: String = "",
    val categoryId: String? = null,
    val category: String = "",
    val expenseDate: Timestamp = Timestamp.now(),
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String = "",
    val totalAmount: Double = 0.0,
    val photoUrl: String? = null,
    val photoPath: String? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "categoryId" to (categoryId ?: ""),
            "category" to category,
            "expenseDate" to expenseDate,
            "startTime" to (startTime ?: ""),
            "endTime" to (endTime ?: ""),
            "description" to description,
            "totalAmount" to totalAmount,
            "photoUrl" to (photoUrl ?: ""),
            "photoPath" to (photoPath ?: "")
        )
    }

    // Helper to convert Timestamp to Date for compatibility
    fun getExpenseDateAsDate(): Date {
        return expenseDate.toDate()
    }
}

