package com.firstproject.prog7313_budgetbuddy.data.dao
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

//Imports
import androidx.lifecycle.LiveData
import androidx.room.*
import com.firstproject.prog7313_budgetbuddy.data.entities.Photo

// Data Access Object (DAO) interface for interacting with the "photos" table
@Dao
interface PhotoDao {

    // Inserts a new photo into the database and returns the newly generated photoId
    @Insert
    suspend fun insertPhoto(photo: Photo): Long

    // Updates an existing photo entry in the database
    @Update
    suspend fun updatePhoto(photo: Photo)

    // Deletes a specific photo entry from the database
    @Delete
    suspend fun deletePhoto(photo: Photo)

    // Retrieves the first photo associated with a specific expenseId
    // Returns null if no photo is found
    @Query("SELECT * FROM photos WHERE expenseId = :expenseId LIMIT 1")
    suspend fun getPhotoByExpenseId(expenseId: Int): Photo?

    // Retrieves a photo by its unique photoId
    // Returns null if the photo does not exist
    @Query("SELECT * FROM photos WHERE photoId = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: Int): Photo?
}
