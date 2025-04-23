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
import com.firstproject.prog7313_budgetbuddy.data.entities.BudgetGoal
import java.util.Date

// DAO (Data Access Object) interface for interacting with the "budget_goals" table.
// This interface defines methods to insert, update, delete, and query budget goals.
@Dao
interface BudgetGoalDao {

    // Inserts a new BudgetGoal record into the database.
    // - Returns the auto-generated ID (primary key) of the inserted row.
    // - Marked as `suspend` to support coroutine-based asynchronous operations.
    @Insert
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    // Updates an existing BudgetGoal entry in the database.
    // - Requires that the entry already exists (matched by primary key).
    // - Marked as `suspend` for background execution in coroutines.
    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal)

    // Deletes a specific BudgetGoal entry from the database.
    // - Only deletes the matching record based on the primary key.
    @Delete
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal)

    // Retrieves the most recent BudgetGoal (based on startDate) for a specific user.
    // - Results are wrapped in LiveData to allow real-time observation in the UI.
    // - Sorted in descending order by startDate, then limits to the most recent one.
    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?>

    // Fetches the budget goal that includes a specific date for a given user.
    // - Useful for determining which goal was active during a particular time.
    // - Uses SQL `BETWEEN` to match the date within the startDate and endDate range.
    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND :date BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getBudgetGoalForDate(userId: String, date: Date): BudgetGoal?

    // Retrieves a BudgetGoal by its unique ID (goalId).
    // - Useful for editing, viewing, or referencing a specific goal.
    @Query("SELECT * FROM budget_goals WHERE goalId = :goalId LIMIT 1")
    suspend fun getBudgetGoalById(goalId: Int): BudgetGoal?
}
