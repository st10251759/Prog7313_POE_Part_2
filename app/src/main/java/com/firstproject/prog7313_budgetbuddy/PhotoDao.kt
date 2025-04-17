package com.firstproject.prog7313_budgetbuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.firstproject.prog7313_budgetbuddy.data.entities.Photo

@Dao
interface PhotoDao {
    @Insert
    suspend fun insertPhoto(photo: Photo): Long

    @Update
    suspend fun updatePhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("SELECT * FROM photos WHERE expenseId = :expenseId LIMIT 1")
    suspend fun getPhotoByExpenseId(expenseId: Int): Photo?

    @Query("SELECT * FROM photos WHERE photoId = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: Int): Photo?
}