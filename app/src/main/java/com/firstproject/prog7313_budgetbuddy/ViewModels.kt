package com.firstproject.prog7313_budgetbuddy.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.firstproject.prog7313_budgetbuddy.BudgetBuddyDatabase
import com.firstproject.prog7313_budgetbuddy.data.entities.*
import com.firstproject.prog7313_budgetbuddy.data.repositories.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ViewModels(application: Application) : AndroidViewModel(application) {
    private val repository: Repositories
    private val auth = FirebaseAuth.getInstance()

    // LiveData for UI updates
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _currentDateRange = MutableLiveData<Pair<Date, Date>>()
    val currentDateRange: LiveData<Pair<Date, Date>> = _currentDateRange

    init {
        val database = BudgetBuddyDatabase.getDatabase(application)
        repository = Repositories(
            database.categoryDao(),
            database.expenseDao(),
            database.photoDao(),
            database.budgetGoalDao()
        )

        // Set current user
        _currentUser.value = auth.currentUser

        // Listen for auth changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }

        // Set default date range to current month
        setCurrentMonthDateRange()
    }

    // Set date range to current month
    private fun setCurrentMonthDateRange() {
        val calendar = Calendar.getInstance()

        // Set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        // Set to last day of month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        _currentDateRange.value = Pair(startDate, endDate)
    }

    // Set custom date range
    fun setCustomDateRange(startDate: Date, endDate: Date) {
        _currentDateRange.value = Pair(startDate, endDate)
    }

    fun loginWithFirebase(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _currentUser.value = auth.currentUser
                onSuccess()
            }
            .addOnFailureListener { onFailure(it.message ?: "Authentication failed") }
    }

    fun registerWithFirebase(email: String, password: String, displayName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Update user profile with display name
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

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    // Get current Firebase user ID
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Category methods
    fun getAllCategories(): LiveData<List<Category>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        return repository.getAllCategoriesByUser(currentUserId)
    }

    fun createCategory(name: String, color: String) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch
        val category = Category(
            userId = currentUserId,
            categoryName = name,
            colour = color
        )
        repository.insertCategory(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    // Expense methods
    fun getAllExpenses(): LiveData<List<Expense>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        return repository.getAllExpensesByUser(currentUserId)
    }

    fun getExpensesForPeriod(): LiveData<List<Expense>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        val dateRange = _currentDateRange.value ?: return MutableLiveData(emptyList())
        return repository.getExpensesByPeriod(currentUserId, dateRange.first, dateRange.second)
    }

    fun getTotalExpensesForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<Double> {
        return repository.getTotalExpensesForPeriod(userId, startDate, endDate)
    }

    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?> {
        return repository.getCurrentBudgetGoal(userId)
    }

    fun createExpense(
        categoryId: Int?,
        categoryName: String,
        expenseDate: Date,
        startTime: String?,
        endTime: String?,
        description: String,
        amount: Double,
        photoPath: String? = null
    ) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch

        // First create expense without photo
        val expense = Expense(
            userId = currentUserId,
            categoryId = categoryId,
            category = categoryName,
            expenseDate = expenseDate,
            startTime = startTime,
            endTime = endTime,
            description = description,
            totalAmount = amount,
            photoId = null
        )

        val expenseId = repository.insertExpense(expense)

        // If photo path is provided, create and link photo
        if (!photoPath.isNullOrEmpty()) {
            val photo = Photo(
                expenseId = expenseId.toInt(),
                filePath = photoPath,
                uploadedAt = Date()
            )

            val photoId = repository.insertPhoto(photo)

            // Update expense with photo ID
            val updatedExpense = expense.copy(
                expenseId = expenseId.toInt(),
                photoId = photoId.toString()
            )

            repository.updateExpense(updatedExpense)
        }
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        // First check if there's a photo to delete
        expense.photoId?.let { photoIdStr ->
            try {
                val photoId = photoIdStr.toInt()
                repository.getPhotoById(photoId)?.let { photo ->
                    repository.deletePhoto(photo)
                }
            } catch (e: NumberFormatException) {
                // Invalid photo ID, ignore
            }
        }

        // Then delete the expense
        repository.deleteExpense(expense)
    }

    fun getExpensesByCategory(categoryId: Int): LiveData<List<Expense>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        return repository.getExpensesByCategory(currentUserId, categoryId)
    }

    fun getTotalExpensesForPeriod(): LiveData<Double> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(0.0)
        val dateRange = _currentDateRange.value ?: return MutableLiveData(0.0)
        return repository.getTotalExpensesForPeriod(currentUserId, dateRange.first, dateRange.second)
    }

    fun updateBudgetGoal(budgetGoal: BudgetGoal) = viewModelScope.launch {
        repository.updateBudgetGoal(budgetGoal)
    }

    fun deleteBudgetGoal(budgetGoal: BudgetGoal) = viewModelScope.launch {
        repository.deleteBudgetGoal(budgetGoal)
    }

    // Photo methods
    suspend fun getPhotoByExpenseId(expenseId: Int): Photo? {
        return repository.getPhotoByExpenseId(expenseId)
    }

    /**
     * Gets expenses for a specific period synchronously.
     * This is used by the ExpenseListActivity.
     */
    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>> {
        return repository.getExpensesByPeriod(userId, startDate, endDate)
    }

    /**
     * Gets a photo by its ID synchronously.
     * Used when accessing receipt photos from the expense list.
     */
    suspend fun getPhotoById(photoId: Int): Photo? {
        return withContext(Dispatchers.IO) {
            repository.getPhotoById(photoId)
        }
    }

    fun getCategorySpendingForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<CategoryWithAmount>> {
        val result = MediatorLiveData<List<CategoryWithAmount>>()

        val totalExpensesLiveData = repository.getTotalExpensesForPeriod(userId, startDate, endDate)
        val categoriesLiveData = repository.getCategorySpendingForPeriod(userId, startDate, endDate)

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

    private fun calculateCategoryWithAmounts(
        categories: List<CategorySpending>,
        totalAmount: Double
    ): List<CategoryWithAmount> {
        return categories.map { categorySpending ->
            CategoryWithAmount(
                categoryId = categorySpending.category.categoryId,
                categoryName = categorySpending.category.categoryName,
                colour = categorySpending.category.colour,
                amount = categorySpending.amount,
                percentage = if (totalAmount > 0) (categorySpending.amount / totalAmount).toFloat() else 0f
            )
        }
    }

    /**
     * Gets the current budget goal for the logged-in user
     */
    fun getCurrentBudgetGoal(): LiveData<BudgetGoal?> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(null)
        return repository.getCurrentBudgetGoal(currentUserId)
    }

    /**
     * Creates a new budget goal for the current month
     */
    fun createBudgetGoal(minAmount: Double, maxAmount: Double, startDate: Date, endDate: Date) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch

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
                startDate = startDate,
                endDate = endDate
            )
            repository.insertBudgetGoal(budgetGoal)
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