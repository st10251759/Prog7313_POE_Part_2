package com.firstproject.prog7313_budgetbuddy.data.entities

import androidx.room.*
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val fullname: String,
    val email: String,
    val password: String
)

