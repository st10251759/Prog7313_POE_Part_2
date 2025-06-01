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

//Imports
import com.google.firebase.Timestamp
import java.util.Date

// BudgetGoal model for Firestore
// Data class representing a user's budget goal within a specific date range.
data class BudgetGoal(
    var id: String = "",
    val userId: String = "",                      // The ID of the user this goal belongs to
    val minGoalAmount: Double = 0.0,             // Minimum target amount to stay above (e.g., save at least this much)
    val maxGoalAmount: Double = 0.0,             // Maximum target amount to stay below (e.g., spend no more than this)
    val startDate: Timestamp = Timestamp.now(),     // Start date of the goal (default is now)
    val endDate: Timestamp = Timestamp.now()         // End date of the goal (default is now)
) {
    // Converts the BudgetGoal instance into a map structure for use with Firebase Firestore
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


    // Converts Firestore Timestamp to Java Date for use with standard Android date functions
    fun getStartDateAsDate(): Date {
        return startDate.toDate()
    }

    // Converts Firestore Timestamp to Java Date for use with standard Android date functions
    fun getEndDateAsDate(): Date {
        return endDate.toDate()
    }
}
