package com.firstproject.prog7313_budgetbuddy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId"), Index("categoryId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Int = 0,
    val userId: String,  // Changed from Int to String to store Firebase UID
    val categoryId: Int?,
    val expenseDate: Date,
    val startTime: String? = null,
    val endTime: String? = null,
    val category: String,
    val description: String,
    val totalAmount: Double,
    val photoId: String? = null
)