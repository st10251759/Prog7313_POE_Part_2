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

/**
 * Data class representing a category with its spending amount for a specific period
 */
data class CategoryWithAmount(
    val categoryId: Int,
    val categoryName: String,
    val colour: String,
    val amount: Double,
    val percentage: Float // Percentage of total spending
)