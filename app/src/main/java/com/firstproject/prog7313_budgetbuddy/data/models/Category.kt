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
    // Empty constructor required for Firestore
    constructor() : this("", "", "", "")

    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "categoryName" to categoryName,
            "colour" to colour
        )
    }
}

