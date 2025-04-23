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


// Import necessary Android, Room and Kotlin libraries
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

// Defines a Room database entity named "photos" with a foreign key relationship
@Entity(
    tableName = "photos", // Name of the table in the database
    foreignKeys = [
        ForeignKey(
            entity = Expense::class, // The parent entity this table references
            parentColumns = ["expenseId"], // Column in the parent table (Expense)
            childColumns = ["expenseId"], // Column in this table referencing the parent
            onDelete = ForeignKey.CASCADE // If the parent is deleted, delete child rows too
        )
    ],
    indices = [Index("expenseId")] // Improves performance for lookups on "expenseId"
)
data class Photo(
    @PrimaryKey(autoGenerate = true) // Automatically generate unique photo IDs
    val photoId: Int = 0, // Primary key for each photo record
    val expenseId: Int, // Foreign key linking this photo to an expense
    val filePath: String, // Path to the photo file stored locally or remotely
    val uploadedAt: Date // Timestamp of when the photo was uploaded
)
