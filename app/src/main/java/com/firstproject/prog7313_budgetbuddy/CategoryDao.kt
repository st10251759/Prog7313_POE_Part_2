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

// Imports required Android and Room database libraries and classes.
import androidx.lifecycle.LiveData
import androidx.room.*
import com.firstproject.prog7313_budgetbuddy.data.entities.Category

// Data Access Object (DAO) for performing database operations on the Category entity
@Dao
interface CategoryDao {

    // Inserts a new category into the database
    // Returns the row ID of the newly inserted item
    @Insert
    suspend fun insertCategory(category: Category): Long

    // Updates an existing category in the database
    @Update
    suspend fun updateCategory(category: Category)

    // Deletes a specified category from the database
    @Delete
    suspend fun deleteCategory(category: Category)

    // Retrieves all categories belonging to a specific user as LiveData
    // LiveData allows the UI to observe changes to the list automatically
    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getAllCategoriesByUser(userId: String): LiveData<List<Category>>

    // Retrieves a single category by its unique ID (returns null if not found)
    @Query("SELECT * FROM categories WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?

    // Retrieves a category by its name and associated user ID (ensures uniqueness per user)
    @Query("SELECT * FROM categories WHERE categoryName = :name AND userId = :userId LIMIT 1")
    suspend fun getCategoryByName(name: String, userId: String): Category?
}