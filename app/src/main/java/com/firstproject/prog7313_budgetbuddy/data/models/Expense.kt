package com.firstproject.prog7313_budgetbuddy.data.models

import com.google.firebase.Timestamp
import java.util.Date

// Expense model for Firestore
data class Expense(
    var id: String = "",
    val userId: String = "",
    val categoryId: String? = null,
    val expenseDate: Timestamp = Timestamp.now(),
    val startTime: String? = null,
    val endTime: String? = null,
    val category: String = "",
    val description: String = "",
    val totalAmount: Double = 0.0,
    val photoUrl: String? = null, // Changed from photoId to photoUrl for Firebase Storage
    val photoPath: String? = null // Local path for display purposes
) {
    // Empty constructor required for Firestore
    constructor() : this("", "", null, Timestamp.now(), null, null, "", "", 0.0, null, null)

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "expenseDate" to expenseDate,
            "category" to category,
            "description" to description,
            "totalAmount" to totalAmount
        )

        categoryId?.let { map["categoryId"] = it }
        startTime?.let { map["startTime"] = it }
        endTime?.let { map["endTime"] = it }
        photoUrl?.let { map["photoUrl"] = it }
        photoPath?.let { map["photoPath"] = it }

        return map
    }

    // Helper to convert Timestamp to Date for compatibility
    fun getExpenseDateAsDate(): Date {
        return expenseDate.toDate()
    }
}

