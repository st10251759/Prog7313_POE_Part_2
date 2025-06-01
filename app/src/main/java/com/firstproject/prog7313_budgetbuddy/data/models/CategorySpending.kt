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


// CategorySpending model for aggregated data
data class CategorySpending(
    val category: Category,
    val amount: Double
)