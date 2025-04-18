package com.firstproject.prog7313_budgetbuddy.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class CategorySpending(
    @Embedded val category: Category,
    val amount: Double
)