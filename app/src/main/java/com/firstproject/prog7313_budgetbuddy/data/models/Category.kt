package com.firstproject.prog7313_budgetbuddy.data.models

import com.google.firebase.Timestamp
import java.util.Date

// Category model for Firestore
data class Category(
    var id: String = "",
    val userId: String = "",
    val categoryName: String = "",
    val colour: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "categoryName" to categoryName,
            "colour" to colour
        )
    }
}
