package com.firstproject.prog7313_budgetbuddy.data.entities

/**
 * Data class representing a category with its spending amount for a specific period
 */
data class CategoryWithAmount(
    val categoryId: Int,
    val categoryName: String,
    val colour: String,
    val amount: Double,
    val percentage: Float // Percentage of total spending
)