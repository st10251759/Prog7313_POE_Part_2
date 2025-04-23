package com.firstproject.prog7313_budgetbuddy.viewmodels
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

/*
 --------------------------------Code Attribution----------------------------------
 Title: ViewModel overview  |  App architecture  |  Android Developers
 Author: Android Developer
 Date Published: 2019
 Date Accessed: 18 April 2025
 Code Version: v21.20
 Availability: https://developer.android.com/topic/libraries/architecture/viewmodel
  --------------------------------Code Attribution----------------------------------
*/

// Import necessary Android, Room and Kotlin libraries
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

// ViewModel class for managing UI-related data in a lifecycle-conscious way
class ViewModels(application: Application) : AndroidViewModel(application) {

    // Repository to handle all database operations
    private val repository: Repositories

    // Firebase Authentication instance
    private val auth = FirebaseAuth.getInstance()

    // LiveData to hold the current logged-in Firebase user
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    // LiveData to track the current selected date range
    private val _currentDateRange = MutableLiveData<Pair<Date, Date>>()
    val currentDateRange: LiveData<Pair<Date, Date>> = _currentDateRange

    // Initialize database, repository, authentication state listener, and default date range
    init {
        val database = BudgetBuddyDatabase.getDatabase(application)
        repository = Repositories(
            database.categoryDao(),
            database.expenseDao(),
            database.photoDao(),
            database.budgetGoalDao()
        )

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

        // Insert expense
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

        // Add photo if provided
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

    // Update an existing expense
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    // Delete an expense and its associated photo
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expense.photoId?.let { photoIdStr ->
            try {
                val photoId = photoIdStr.toInt()
                repository.getPhotoById(photoId)?.let { photo ->
                    repository.deletePhoto(photo)
                }
            } catch (e: NumberFormatException) {
                // Handle invalid photo ID
            }
        }
        repository.deleteExpense(expense)
    }

    // Get all expenses by category
    fun getExpensesByCategory(categoryId: Int): LiveData<List<Expense>> {
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

    // Get a photo by expense ID
    suspend fun getPhotoByExpenseId(expenseId: Int): Photo? {
        return repository.getPhotoByExpenseId(expenseId)
    }

    // Get expenses by period for specific user (used in activities)
    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>> {
        return repository.getExpensesByPeriod(userId, startDate, endDate)
    }

    // Get photo by ID (used when accessing receipts)
    suspend fun getPhotoById(photoId: Int): Photo? {
        return withContext(Dispatchers.IO) {
            repository.getPhotoById(photoId)
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
                categoryId = categorySpending.category.categoryId,
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