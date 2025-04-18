package com.firstproject.prog7313_budgetbuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.firstproject.prog7313_budgetbuddy.data.entities.Category
import com.firstproject.prog7313_budgetbuddy.data.entities.CategorySpending
import com.firstproject.prog7313_budgetbuddy.data.entities.Expense
import java.util.Date

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY expenseDate DESC")
    fun getAllExpensesByUser(userId: String): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND expenseDate BETWEEN :startDate AND :endDate ORDER BY expenseDate DESC")
    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Int): Expense?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND categoryId = :categoryId")
    fun getExpensesByCategory(userId: String, categoryId: Int): LiveData<List<Expense>>

    @Query("SELECT SUM(totalAmount) FROM expenses WHERE userId = :userId AND expenseDate BETWEEN :startDate AND :endDate")
    fun getTotalExpensesForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<Double>

    /**
     * Gets the total spending amount for each category within a specific time period
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