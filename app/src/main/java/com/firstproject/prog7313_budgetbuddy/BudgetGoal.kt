package com.firstproject.prog7313_budgetbuddy.data.entities

/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 2
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: Save data in a local database using Room  |  App data and files  |  Android Developers
 Author: Android Developer
 Date Published: 2019
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://developer.android.com/training/data-storage/room
  --------------------------------Code Attribution----------------------------------
*/


// Imports required Android and Room database libraries and classes.
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

// Declares this class as a Room database entity (table).
// - The table will be named "budget_goals" in the SQLite database.
// - An index is created on the "userId" column to optimize queries that filter or join by userId.

@Entity(
    tableName = "budget_goals",
    indices = [Index("userId")]
)
data class BudgetGoal(
    // Primary key for the table.
    // - `autoGenerate = true` means Room will automatically assign and increment unique IDs.
    @PrimaryKey(autoGenerate = true)
    val goalId: Int = 0,
    // Identifier linking this goal to a specific user.
    val userId: String,
    val minGoalAmount: Double,
    val maxGoalAmount: Double,
    val startDate: Date,
    val endDate: Date
)