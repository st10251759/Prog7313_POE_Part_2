package com.firstproject.prog7313_budgetbuddy.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.firstproject.prog7313_budgetbuddy.data.models.*
import com.firstproject.prog7313_budgetbuddy.data.repositories.FirestoreRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ViewModels(application: Application) : AndroidViewModel(application) {

    // Repository to handle all database operations
    private val repository = FirestoreRepository()

    // Firebase Authentication instance
    private val auth = FirebaseAuth.getInstance()

    // LiveData to hold the current logged-in Firebase user
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    // LiveData to track the current selected date range
    private val _currentDateRange = MutableLiveData<Pair<Date, Date>>()
    val currentDateRange: LiveData<Pair<Date, Date>> = _currentDateRange

    // Initialize authentication state listener and default date range
    init {
        // Set initial user state
        _currentUser.value = auth.currentUser

        // Update user state on authentication changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }

        // Set default date range to the current month
        setCurrentMonthDateRange()
    }

    // Helper function to set the date range to the current month
    private fun setCurrentMonthDateRange() {
        val calendar = Calendar.getInstance()

        // Start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        // End of the month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        // Update LiveData with new date range
        _currentDateRange.value = Pair(startDate, endDate)
    }

    // Function to allow setting a custom date range
    fun setCustomDateRange(startDate: Date, endDate: Date) {
        _currentDateRange.value = Pair(startDate, endDate)
    }

    // Login function using Firebase Authentication
    fun loginWithFirebase(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _currentUser.value = auth.currentUser
                onSuccess()
            }
            .addOnFailureListener { onFailure(it.message ?: "Authentication failed") }
    }

    // Register a new user and update their display name
    fun registerWithFirebase(email: String, password: String, displayName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                authResult.user?.updateProfile(profileUpdates)
                    ?.addOnSuccessListener {
                        _currentUser.value = auth.currentUser
                        onSuccess()
                    }
                    ?.addOnFailureListener { onFailure(it.message ?: "Failed to update profile") }
            }
            .addOnFailureListener { onFailure(it.message ?: "Registration failed") }
    }

    // Sign out current user
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    // Get the currently logged-in user ID
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // ------------------------ Category Methods ------------------------

    // Fetch all categories for the logged-in user
    fun getAllCategories(): LiveData<List<Category>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        return repository.getAllCategoriesByUser(currentUserId)
    }

    // Create a new category
    fun createCategory(name: String, color: String) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch
        val category = Category(
            userId = currentUserId,
            categoryName = name,
            colour = color
        )
        repository.insertCategory(category)
    }

    // Update an existing category
    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategory(category)
    }

    // Delete a category
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    // ------------------------ Expense Methods ------------------------

    // Fetch all expenses for the logged-in user
    fun getAllExpenses(): LiveData<List<Expense>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        return repository.getAllExpensesByUser(currentUserId)
    }

    // Get expenses within the current date range
    fun getExpensesForPeriod(): LiveData<List<Expense>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        val dateRange = _currentDateRange.value ?: return MutableLiveData(emptyList())
        return repository.getExpensesByPeriod(currentUserId, dateRange.first, dateRange.second)
    }

    // Get the total expense amount for a given period
    fun getTotalExpensesForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<Double> {
        return repository.getTotalExpensesForPeriod(userId, startDate, endDate)
    }

    // Get the current budget goal for a specific user
    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?> {
        return repository.getCurrentBudgetGoal(userId)
    }

    // Create a new expense, optionally with a photo
    fun createExpense(
        categoryId: String?,
        categoryName: String,
        expenseDate: Date,
        startTime: String?,
        endTime: String?,
        description: String,
        amount: Double,
        photoPath: String? = null
    ) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch

        try {
            // Upload photo if provided
            var photoUrl: String? = null
            if (!photoPath.isNullOrEmpty()) {
                photoUrl = repository.uploadPhoto(photoPath, currentUserId)
            }

            // Create expense
            val expense = Expense(
                userId = currentUserId,
                categoryId = categoryId,
                category = categoryName,
                expenseDate = Timestamp(expenseDate),
                startTime = startTime,
                endTime = endTime,
                description = description,
                totalAmount = amount,
                photoUrl = photoUrl,
                photoPath = photoPath
            )

            repository.insertExpense(expense)
        } catch (e: Exception) {
            // Handle error - you might want to show this to the user
            android.util.Log.e("ViewModels", "Error creating expense", e)
        }
    }

    // Update an existing expense
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    // Delete an expense and its associated photo
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // Get all expenses by category
    fun getExpensesByCategory(categoryId: String): LiveData<List<Expense>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        return repository.getExpensesByCategory(currentUserId, categoryId)
    }

    // Get total expenses within current date range
    fun getTotalExpensesForPeriod(): LiveData<Double> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(0.0)
        val dateRange = _currentDateRange.value ?: return MutableLiveData(0.0)
        return repository.getTotalExpensesForPeriod(currentUserId, dateRange.first, dateRange.second)
    }

    // Update budget goal
    fun updateBudgetGoal(budgetGoal: BudgetGoal) = viewModelScope.launch {
        repository.updateBudgetGoal(budgetGoal)
    }

    // Delete budget goal
    fun deleteBudgetGoal(budgetGoal: BudgetGoal) = viewModelScope.launch {
        repository.deleteBudgetGoal(budgetGoal)
    }

    // ------------------------ Photo Methods ------------------------

    // Get expenses by period for specific user (used in activities)
    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>> {
        return repository.getExpensesByPeriod(userId, startDate, endDate)
    }

    // Get expense by ID (for photo access)
    suspend fun getExpenseById(expenseId: String): Expense? {
        return withContext(Dispatchers.IO) {
            repository.getExpenseById(expenseId)
        }
    }

    // Get spending per category for a specific period and calculate percentage of total
    fun getCategorySpendingForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<CategoryWithAmount>> {
        val result = MediatorLiveData<List<CategoryWithAmount>>()

        val totalExpensesLiveData = repository.getTotalExpensesForPeriod(userId, startDate, endDate)
        val categoriesLiveData = repository.getCategorySpendingForPeriod(userId, startDate, endDate)

        // Observe both total and categories to compute percentages
        result.addSource(totalExpensesLiveData) { totalAmount ->
            val categories = categoriesLiveData.value
            if (categories != null) {
                result.value = calculateCategoryWithAmounts(categories, totalAmount ?: 0.0)
            }
        }

        result.addSource(categoriesLiveData) { categories ->
            val totalAmount = totalExpensesLiveData.value
            if (totalAmount != null) {
                result.value = calculateCategoryWithAmounts(categories, totalAmount)
            }
        }

        return result
    }

    // Helper function to calculate category percentage from total spending
    private fun calculateCategoryWithAmounts(
        categories: List<CategorySpending>,
        totalAmount: Double
    ): List<CategoryWithAmount> {
        return categories.map { categorySpending ->
            CategoryWithAmount(
                categoryId = categorySpending.category.id,
                categoryName = categorySpending.category.categoryName,
                colour = categorySpending.category.colour,
                amount = categorySpending.amount,
                percentage = if (totalAmount > 0) (categorySpending.amount / totalAmount).toFloat() else 0f
            )
        }
    }

    // Get budget goal for the current user
    fun getCurrentBudgetGoal(): LiveData<BudgetGoal?> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(null)
        return repository.getCurrentBudgetGoal(currentUserId)
    }

    // Create a new budget goal
    fun createBudgetGoal(minAmount: Double, maxAmount: Double, startDate: Date, endDate: Date) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch

        try {
            // Check if a budget goal already exists for this period
            val existingGoal = repository.getBudgetGoalForDate(currentUserId, startDate)

            if (existingGoal != null) {
                // Update existing goal
                val updatedGoal = existingGoal.copy(
                    minGoalAmount = minAmount,
                    maxGoalAmount = maxAmount
                )
                repository.updateBudgetGoal(updatedGoal)
            } else {
                // Create new goal
                val budgetGoal = BudgetGoal(
                    userId = currentUserId,
                    minGoalAmount = minAmount,
                    maxGoalAmount = maxAmount,
                    startDate = Timestamp(startDate),
                    endDate = Timestamp(endDate)
                )
                repository.insertBudgetGoal(budgetGoal)
            }
        } catch (e: Exception) {
            android.util.Log.e("ViewModels", "Error creating/updating budget goal", e)
        }
    }

    /**
     * Gets budget goal for a specific date
     */
    suspend fun getBudgetGoalForDate(date: Date): BudgetGoal? {
        val currentUserId = getCurrentUserId() ?: return null
        return repository.getBudgetGoalForDate(currentUserId, date)
    }

    /**
     * Gets budget goal progress - comparing current spending to min/max goals
     * Returns a Triple of (currentAmount, minGoal, maxGoal)
     */
    fun getBudgetProgress(startDate: Date, endDate: Date): LiveData<Triple<Double, Double, Double>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(Triple(0.0, 0.0, 0.0))

        val result = MediatorLiveData<Triple<Double, Double, Double>>()

        val spendingLiveData = repository.getTotalExpensesForPeriod(currentUserId, startDate, endDate)
        val budgetGoalLiveData = repository.getCurrentBudgetGoal(currentUserId)

        result.addSource(spendingLiveData) { spending ->
            val budgetGoal = budgetGoalLiveData.value
            if (budgetGoal != null) {
                result.value = Triple(
                    spending ?: 0.0,
                    budgetGoal.minGoalAmount,
                    budgetGoal.maxGoalAmount
                )
            }
        }

        result.addSource(budgetGoalLiveData) { budgetGoal ->
            val spending = spendingLiveData.value
            if (budgetGoal != null) {
                result.value = Triple(
                    spending ?: 0.0,
                    budgetGoal.minGoalAmount,
                    budgetGoal.maxGoalAmount
                )
            }
        }

        return result
    }
}