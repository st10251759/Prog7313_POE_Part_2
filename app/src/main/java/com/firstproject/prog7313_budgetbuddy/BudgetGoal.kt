package com.firstproject.prog7313_budgetbuddy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "budget_goals",
    indices = [Index("userId")]
)
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    val goalId: Int = 0,
    val userId: String,  // Changed from Int to String
    val minGoalAmount: Double,
    val maxGoalAmount: Double,
    val startDate: Date,
    val endDate: Date
)