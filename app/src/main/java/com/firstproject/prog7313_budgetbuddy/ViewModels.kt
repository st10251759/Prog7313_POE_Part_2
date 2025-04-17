package com.firstproject.prog7313_budgetbuddy.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.firstproject.prog7313_budgetbuddy.BudgetBuddyDatabase
import com.firstproject.prog7313_budgetbuddy.data.entities.*
import com.firstproject.prog7313_budgetbuddy.data.repositories.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
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

    // Budget Goal methods
    fun getCurrentBudgetGoal(): LiveData<BudgetGoal?> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(null)
        return repository.getCurrentBudgetGoal(currentUserId)
    }

    fun createBudgetGoal(minAmount: Double, maxAmount: Double, startDate: Date, endDate: Date) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch
        val budgetGoal = BudgetGoal(
            userId = currentUserId,
            minGoalAmount = minAmount,
            maxGoalAmount = maxAmount,
            startDate = startDate,
            endDate = endDate
        )
        repository.insertBudgetGoal(budgetGoal)
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
}