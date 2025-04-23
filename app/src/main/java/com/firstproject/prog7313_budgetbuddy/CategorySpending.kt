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
import androidx.room.Embedded
import androidx.room.Relation

// A data class used to represent a category along with its total spending amount.
// Typically used for custom queries where you want to join category data with aggregated spending.
data class CategorySpending(

    // Embeds the Category entity fields directly into this data class.
    // Room will treat the properties of Category as if they were fields in this class.
    @Embedded val category: Category,

    // Represents the total amount spent in the associated category.
    val amount: Double
)