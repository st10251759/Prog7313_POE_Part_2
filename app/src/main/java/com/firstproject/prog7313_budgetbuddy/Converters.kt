package com.firstproject.prog7313_budgetbuddy.data

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
 Title: Referencing complex data using Room  |  App data and files  |  Android Developers
 Author: Android Developer
 Date Published: 2019
 Date Accessed: 19 April 2025
 Code Version: v21.20
 Availability: https://developer.android.com/training/data-storage/room/referencing-data
  --------------------------------Code Attribution----------------------------------
*/
// Import necessary Kotlin, Date and Room libraries
import androidx.room.TypeConverter
import java.util.Date

// This class provides methods to convert custom types for Room database storage
// Specifically, it converts between Date and Long (timestamp), since Room doesn't support Date directly.
class Converters {
    @TypeConverter
    // Converts a Long (timestamp) value from the database to a Date object
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) } // If value is not null, return a Date object based on the timestamp
    }
    // Converts a Date object to a Long (timestamp) so it can be stored in the database
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time  // If date is not null, return its time in milliseconds
    }
}



