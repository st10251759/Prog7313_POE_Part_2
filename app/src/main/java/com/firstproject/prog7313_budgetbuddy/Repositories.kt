package com.firstproject.prog7313_budgetbuddy.data.repositories

import androidx.lifecycle.LiveData
import com.firstproject.prog7313_budgetbuddy.data.dao.*
import com.firstproject.prog7313_budgetbuddy.data.entities.*
import java.util.*

class Repositories(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val photoDao: PhotoDao,
    private val budgetGoalDao: BudgetGoalDao
) {
    // Category Repository Methods
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    fun getAllCategoriesByUser(userId: String): LiveData<List<Category>> {
        return categoryDao.getAllCategoriesByUser(userId)
    }

    suspend fun getCategoryById(categoryId: Int): Category? {
        return categoryDao.getCategoryById(categoryId)
    }

    suspend fun getCategoryByName(name: String, userId: String): Category? {
        return categoryDao.getCategoryByName(name, userId)
    }

    // Expense Repository Methods
    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    fun getAllExpensesByUser(userId: String): LiveData<List<Expense>> {
        return expenseDao.getAllExpensesByUser(userId)
    }

    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>> {
        return expenseDao.getExpensesByPeriod(userId, startDate, endDate)
    }

    suspend fun getExpenseById(expenseId: Int): Expense? {
        return expenseDao.getExpenseById(expenseId)
    }

    fun getExpensesByCategory(userId: String, categoryId: Int): LiveData<List<Expense>> {
        return expenseDao.getExpensesByCategory(userId, categoryId)
    }

    fun getTotalExpensesForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<Double> {
        return expenseDao.getTotalExpensesForPeriod(userId, startDate, endDate)
    }

    // Photo Repository Methods (unchanged)
    suspend fun insertPhoto(photo: Photo): Long {
        return photoDao.insertPhoto(photo)
    }

    suspend fun updatePhoto(photo: Photo) {
        photoDao.updatePhoto(photo)
    }

    suspend fun deletePhoto(photo: Photo) {
        photoDao.deletePhoto(photo)
    }

    suspend fun getPhotoByExpenseId(expenseId: Int): Photo? {
        return photoDao.getPhotoByExpenseId(expenseId)
    }

    suspend fun getPhotoById(photoId: Int): Photo? {
        return photoDao.getPhotoById(photoId)
    }

    // Budget Goal Repository Methods
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long {
        return budgetGoalDao.insertBudgetGoal(budgetGoal)
    }

    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal) {
        budgetGoalDao.updateBudgetGoal(budgetGoal)
    }

    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal) {
        budgetGoalDao.deleteBudgetGoal(budgetGoal)
    }

    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?> {
        return budgetGoalDao.getCurrentBudgetGoal(userId)
    }

    suspend fun getBudgetGoalForDate(userId: String, date: Date): BudgetGoal? {
        return budgetGoalDao.getBudgetGoalForDate(userId, date)
    }

    suspend fun getBudgetGoalById(goalId: Int): BudgetGoal? {
        return budgetGoalDao.getBudgetGoalById(goalId)
    }
}