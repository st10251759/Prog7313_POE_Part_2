package com.firstproject.prog7313_budgetbuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.firstproject.prog7313_budgetbuddy.data.entities.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getAllCategoriesByUser(userId: String): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?

    @Query("SELECT * FROM categories WHERE categoryName = :name AND userId = :userId LIMIT 1")
    suspend fun getCategoryByName(name: String, userId: String): Category?
}