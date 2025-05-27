package com.firstproject.prog7313_budgetbuddy.data.models

// CategoryWithAmount for spending analysis (calculated client-side)
data class CategoryWithAmount(
    val categoryId: String,
    val categoryName: String,
    val colour: String,
    val amount: Double,
    val percentage: Float
)