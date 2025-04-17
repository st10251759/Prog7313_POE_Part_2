package com.firstproject.prog7313_budgetbuddy.data.repositories

import androidx.lifecycle.LiveData
import com.firstproject.prog7313_budgetbuddy.data.dao.*
import com.firstproject.prog7313_budgetbuddy.data.entities.*
import java.util.*

class Repositories(private val userDao: UserDao) {
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }
}


