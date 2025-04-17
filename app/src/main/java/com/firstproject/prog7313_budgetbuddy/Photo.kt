package com.firstproject.prog7313_budgetbuddy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId")]
)
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val photoId: Int = 0,
    val expenseId: Int,
    val filePath: String,
    val uploadedAt: Date
)