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
import java.util.Date

// Expense model for Firestore
// Represents a single expense entry made by a user
data class Expense(
    var id: String = "",                        // Unique ID of the expense (auto-generated or Firestore ID)
    val userId: String = "",                    // ID of the user who made the expense
    val categoryId: String? = null,             // Optional: ID of the category (linked to a Category object)
    val category: String = "",                  // Name of the category (e.g., "Food", "Transport")
    val expenseDate: Timestamp = Timestamp.now(), // Date and time when the expense occurred (Firestore Timestamp)
    val startTime: String? = null,              // Optional: Start time (useful for time-based tracking)
    val endTime: String? = null,                // Optional: End time (used with startTime for duration)
    val description: String = "",               // Short description or notes about the expense
    val totalAmount: Double = 0.0,              // Total amount of money spent
    val photoUrl: String? = null,               // Optional: URL to a photo receipt or item
    val photoPath: String? = null               // Optional: Path in Firebase Storage for image retrieval/deletion
) {

    // Converts this Expense object into a Map<String, Any> for Firestore database storage
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "categoryId" to (categoryId ?: ""),  // If categoryId is null, store an empty string
            "category" to category,
            "expenseDate" to expenseDate,
            "startTime" to (startTime ?: ""),    // If startTime is null, store an empty string
            "endTime" to (endTime ?: ""),        // If endTime is null, store an empty string
            "description" to description,
            "totalAmount" to totalAmount,
            "photoUrl" to (photoUrl ?: ""),      // If photoUrl is null, store an empty string
            "photoPath" to (photoPath ?: "")     // If photoPath is null, store an empty string
        )
    }

    // Converts the Firestore Timestamp to a Java Date object (useful for formatting or comparing dates)
    fun getExpenseDateAsDate(): Date {
        return expenseDate.toDate()
    }
}
