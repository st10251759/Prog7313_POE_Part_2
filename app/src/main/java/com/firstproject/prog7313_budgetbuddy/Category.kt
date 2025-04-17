package com.firstproject.prog7313_budgetbuddy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index("userId")]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val userId: String,  // Changed from Int to String
    val categoryName: String,
    val colour: String
)