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
import com.firstproject.prog7313_budgetbuddy.data.entities.Category
import com.firstproject.prog7313_budgetbuddy.data.entities.CategorySpending
import com.firstproject.prog7313_budgetbuddy.data.entities.Expense
import java.util.Date

// Data Access Object (DAO) for performing database operations on the 'expenses' table
@Dao
interface ExpenseDao {
    // Inserts a new expense record and returns the newly generated expenseId
    @Insert
    suspend fun insertExpense(expense: Expense): Long
    // Updates an existing expense record in the database
    @Update
    suspend fun updateExpense(expense: Expense)
    // Deletes a specific expense record from the database
    @Delete
    suspend fun deleteExpense(expense: Expense)
    // Retrieves all expenses for a specific user, sorted by date in descending order (latest first)
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY expenseDate DESC")
    fun getAllExpensesByUser(userId: String): LiveData<List<Expense>>
    // Retrieves expenses for a specific user within a defined date range
    @Query("SELECT * FROM expenses WHERE userId = :userId AND expenseDate BETWEEN :startDate AND :endDate ORDER BY expenseDate DESC")
    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>>
    // Retrieves a single expense by its unique ID (returns null if not found)
    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Int): Expense?
    // Retrieves all expenses under a specific category for a user
    @Query("SELECT * FROM expenses WHERE userId = :userId AND categoryId = :categoryId")
    fun getExpensesByCategory(userId: String, categoryId: Int): LiveData<List<Expense>>
    // Calculates the total amount of expenses for a user within a specified date range
    @Query("SELECT SUM(totalAmount) FROM expenses WHERE userId = :userId AND expenseDate BETWEEN :startDate AND :endDate")
    fun getTotalExpensesForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<Double>

    /**
     * Gets the total spending amount per category within a specific period.
     * This is useful for displaying spending distribution or generating pie/bar charts.
     */
    @Query("""
    SELECT c.*, SUM(e.totalAmount) as amount 
    FROM categories c
    JOIN expenses e ON c.categoryId = e.categoryId
    WHERE e.userId = :userId AND e.expenseDate BETWEEN :startDate AND :endDate
    GROUP BY c.categoryId
    ORDER BY amount DESC
    """)
    fun getCategorySpendingForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<CategorySpending>>

}