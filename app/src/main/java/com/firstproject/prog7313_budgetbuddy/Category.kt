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

// Marks this class as a Room Entity representing a table named "categories" in the database
@Entity(
    tableName = "categories",
    indices = [Index("userId")] // Creates an index on the userId column to optimize queries filtering by user
)
data class Category(
    @PrimaryKey(autoGenerate = true) // categoryId is the primary key and will auto-increment
    val categoryId: Int = 0,

    val userId: String,  // Stores the unique identifier of the user who owns this category

    val categoryName: String, // The name or label of the category (e.g., "Groceries", "Transport")

    val colour: String // A string representing the color code (e.g., hex value) to visually distinguish categories
)