package com.firstproject.prog7313_budgetbuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.firstproject.prog7313_budgetbuddy.data.entities.BudgetGoal
import java.util.Date

@Dao
interface BudgetGoalDao {
    @Insert
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal)

    @Delete
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND :date BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getBudgetGoalForDate(userId: String, date: Date): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE goalId = :goalId LIMIT 1")
    suspend fun getBudgetGoalById(goalId: Int): BudgetGoal?
}