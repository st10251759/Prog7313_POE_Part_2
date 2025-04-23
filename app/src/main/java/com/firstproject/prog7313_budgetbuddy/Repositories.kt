package com.firstproject.prog7313_budgetbuddy.data.repositories

/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 2
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: Save data in a local database using Room  |  App data and files  |  Android Developers
 Author: Android Developer
 Date Published: 2019
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://developer.android.com/training/data-storage/room
  --------------------------------Code Attribution----------------------------------
*/

// Import necessary Android, Room and Kotlin libraries

import androidx.lifecycle.LiveData
import com.firstproject.prog7313_budgetbuddy.data.dao.*
import com.firstproject.prog7313_budgetbuddy.data.entities.*
import java.util.*

// Repository class that serves as an abstraction layer between ViewModel and DAOs
class Repositories(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val photoDao: PhotoDao,
    private val budgetGoalDao: BudgetGoalDao
) {

    // ---------------------- Category Repository Methods ----------------------

    // Inserts a new category into the database
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    // Updates an existing category in the database
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    // Deletes a specific category from the database
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // Retrieves all categories associated with a specific user
    fun getAllCategoriesByUser(userId: String): LiveData<List<Category>> {
        return categoryDao.getAllCategoriesByUser(userId)
    }

    // Retrieves a category by its unique ID
    suspend fun getCategoryById(categoryId: Int): Category? {
        return categoryDao.getCategoryById(categoryId)
    }

    // Retrieves a category by its name and user ID
    suspend fun getCategoryByName(name: String, userId: String): Category? {
        return categoryDao.getCategoryByName(name, userId)
    }

    // ---------------------- Expense Repository Methods ----------------------

    // Inserts a new expense into the database
    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    // Updates an existing expense in the database
    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    // Deletes a specific expense from the database
    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    // Retrieves all expenses for a specific user
    fun getAllExpensesByUser(userId: String): LiveData<List<Expense>> {
        return expenseDao.getAllExpensesByUser(userId)
    }

    // Retrieves expenses for a user within a specific date range
    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>> {
        return expenseDao.getExpensesByPeriod(userId, startDate, endDate)
    }

    // Retrieves a single expense by its unique ID
    suspend fun getExpenseById(expenseId: Int): Expense? {
        return expenseDao.getExpenseById(expenseId)
    }

    // Retrieves expenses for a user that fall under a specific category
    fun getExpensesByCategory(userId: String, categoryId: Int): LiveData<List<Expense>> {
        return expenseDao.getExpensesByCategory(userId, categoryId)
    }

    // Gets the total amount of expenses for a user within a given period
    fun getTotalExpensesForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<Double> {
        return expenseDao.getTotalExpensesForPeriod(userId, startDate, endDate)
    }

    // ---------------------- Photo Repository Methods ----------------------

    // Inserts a photo associated with an expense into the database
    suspend fun insertPhoto(photo: Photo): Long {
        return photoDao.insertPhoto(photo)
    }

    // Updates a photo entry in the database
    suspend fun updatePhoto(photo: Photo) {
        photoDao.updatePhoto(photo)
    }

    // Deletes a photo record from the database
    suspend fun deletePhoto(photo: Photo) {
        photoDao.deletePhoto(photo)
    }

    // Retrieves a photo linked to a specific expense ID
    suspend fun getPhotoByExpenseId(expenseId: Int): Photo? {
        return photoDao.getPhotoByExpenseId(expenseId)
    }

    // Retrieves a photo by its unique ID
    suspend fun getPhotoById(photoId: Int): Photo? {
        return photoDao.getPhotoById(photoId)
    }

    // ---------------------- Budget Goal Repository Methods ----------------------

    // Inserts a new budget goal for a user
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long {
        return budgetGoalDao.insertBudgetGoal(budgetGoal)
    }

    // Updates an existing budget goal
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal) {
        budgetGoalDao.updateBudgetGoal(budgetGoal)
    }

    // Deletes a budget goal from the database
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal) {
        budgetGoalDao.deleteBudgetGoal(budgetGoal)
    }

    // Retrieves the current budget goal for a user
    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?> {
        return budgetGoalDao.getCurrentBudgetGoal(userId)
    }

    // Retrieves a user's budget goal for a specific date
    suspend fun getBudgetGoalForDate(userId: String, date: Date): BudgetGoal? {
        return budgetGoalDao.getBudgetGoalForDate(userId, date)
    }

    // Retrieves a budget goal by its unique ID
    suspend fun getBudgetGoalById(goalId: Int): BudgetGoal? {
        return budgetGoalDao.getBudgetGoalById(goalId)
    }

    /**
     * Retrieves category-wise spending summaries for a specific user and date range.
     */
    fun getCategorySpendingForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<CategorySpending>> {
        return expenseDao.getCategorySpendingForPeriod(userId, startDate, endDate)
    }
}
