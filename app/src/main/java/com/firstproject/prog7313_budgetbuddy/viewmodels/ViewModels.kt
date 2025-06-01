package com.firstproject.prog7313_budgetbuddy.viewmodels

/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 3
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: How to Integrate Firebase Firestore with Kotlin and Use it in Android Apps
 Author: Finotes
 Date Published: 27 June 2023
 Date Accessed: 20 May 2025
 Code Version: N/A
 Availability: https://www.blog.finotes.com/post/how-to-integrate-firebase-firestore-with-kotlin-and-use-it-in-android-apps

 Title: Cloud Firestore Android Kotlin
 Author: Vlad Voytenko
 Date Published: 10 November 2021
 Date Accessed: 20 May 2025
 Code Version: N/A
 Availability: https://www.youtube.com/watch?v=lQv8pkOWnVM

 Title: Enum classes
 Author: Kotlin
 Date Published: 25 September 2024
 Date Accessed: 20 May 2025
 Code Version: v2.1.21
 Availability: https://kotlinlang.org/docs/enum-classes.html#
  --------------------------------Code Attribution----------------------------------
*/

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

    // Add these methods to your ViewModels class

    // Initialize user streak if it doesn't exist
    fun initializeUserStreak() = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch

        try {
            android.util.Log.d("ViewModels", "Initializing user streak for: $currentUserId")

            // Check if streak already exists
            val existingStreak = repository.getUserStreak(currentUserId)

            if (existingStreak == null) {
                // Create new streak with default values
                val newStreak = UserStreak(
                    id = currentUserId,
                    userId = currentUserId,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastLogDate = com.google.firebase.Timestamp(Date(0)), // Set to epoch
                    totalDaysLogged = 0,
                    badges = emptyList(),
                    points = 0,
                    totalExpensesLogged = 0,
                    categoriesUsed = emptyList(),
                    earlyBirdCount = 0,
                    weekendLogCount = 0,
                    budgetKeeperDays = 0,
                    perfectWeeks = 0,
                    lastStreakBreak = null,
                    achievements = emptyList()
                )

                repository.createOrUpdateUserStreak(newStreak)
                android.util.Log.d("ViewModels", "New user streak initialized")
            } else {
                android.util.Log.d("ViewModels", "User streak already exists")
            }
        } catch (e: Exception) {
            android.util.Log.e("ViewModels", "Error initializing user streak", e)
        }
    }

    // Get all available badges
    fun getAllBadges(): List<Badge> {
        return Badge.getAllBadges()
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

    // Modified createExpense method with improved streak update
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
            android.util.Log.d("ViewModels", "Creating expense for user: $currentUserId")

            // Upload photo if provided
            var photoUrl: String? = null
            if (!photoPath.isNullOrEmpty()) {
                android.util.Log.d("ViewModels", "Uploading photo: $photoPath")
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

            android.util.Log.d("ViewModels", "Inserting expense into Firestore")
            repository.insertExpense(expense)

            // Update user streak with expense date and category info
            android.util.Log.d("ViewModels", "Updating user streak after expense log")
            updateStreakOnExpenseLog(expenseDate, categoryId)

        } catch (e: Exception) {
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

    // ------------------------ Gamification Methods ------------------------

    // Get user streak data
    fun getUserStreak(): LiveData<UserStreak?> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(null)
        return repository.observeUserStreak(currentUserId)
    }



    // Update streak method with additional parameters
    private suspend fun updateStreakOnExpenseLog(expenseDate: Date, categoryId: String?) {
        val currentUserId = getCurrentUserId() ?: return

        try {
            android.util.Log.d("ViewModels", "=== UPDATING STREAK ON EXPENSE LOG ===")
            android.util.Log.d("ViewModels", "User: $currentUserId, Date: $expenseDate, Category: $categoryId")

            // Add retry logic for streak updates
            var retryCount = 0
            val maxRetries = 3

            while (retryCount < maxRetries) {
                try {
                    val result = repository.updateUserStreakOnExpenseLog(currentUserId, expenseDate, categoryId)
                    if (result != null) {
                        android.util.Log.d("ViewModels", "Streak update successful on attempt ${retryCount + 1}")
                        android.util.Log.d("ViewModels", "New streak: ${result.currentStreak} days, ${result.totalExpensesLogged} expenses")
                        break
                    } else {
                        throw Exception("Streak update returned null")
                    }
                } catch (e: Exception) {
                    retryCount++
                    android.util.Log.w("ViewModels", "Streak update attempt $retryCount failed: ${e.message}")

                    if (retryCount >= maxRetries) {
                        android.util.Log.e("ViewModels", "Streak update failed after $maxRetries attempts")
                        throw e
                    } else {
                        // Wait before retry
                        kotlinx.coroutines.delay(1000L * retryCount)
                    }
                }
            }

            android.util.Log.d("ViewModels", "=== STREAK UPDATE COMPLETED ===")
        } catch (e: Exception) {
            android.util.Log.e("ViewModels", "Critical error updating streak: ${e.message}", e)
            // Don't throw here - we don't want to prevent expense creation due to streak update issues
        }
    }

    // Get achievement progress for different badge types
    /**
     * Retrieves the user's progress towards earning badges, categorized by badge type,
     * and returns it as a LiveData map where each BadgeType maps to a Float value representing progress.
     * The progress value is a ratio (0.0 to 1.0) indicating how close the user is to achieving the next badge
     * within that badge type.
     */
    fun getAchievementProgress(): LiveData<Map<BadgeType, Float>> {
        // Attempt to get the current user's ID.
        // If no user is logged in (null), return an empty LiveData immediately.
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyMap())

        // Initialize a MutableLiveData to hold and emit the progress map.
        // This LiveData will be observed by the UI or other components interested in progress updates.
        val result = MutableLiveData<Map<BadgeType, Float>>()

        // Observe the user's streak data as LiveData.
        // This will keep the progress updated in real-time whenever the streak data changes.
        getUserStreak().observeForever { userStreak ->
            // Only proceed if the user streak data is not null.
            if (userStreak != null) {
                // Create a mutable map to store progress values for each badge type.
                // The key is BadgeType and the value is a Float representing progress percentage (0.0 - 1.0).
                val progress = mutableMapOf<BadgeType, Float>()

                // Retrieve the complete list of badges available in the system.
                val allBadges = Badge.getAllBadges()

                // Loop through all possible BadgeTypes defined in the enum.
                BadgeType.values().forEach { badgeType ->
                    // Filter the badges to only those that belong to the current badgeType being processed.
                    val badgesOfType = allBadges.filter { it.badgeType == badgeType }

                    // Find the next badge the user has not yet earned for this badge type.
                    // This assumes badges are ordered so the first missing badge is the next target.
                    val nextBadge = badgesOfType.firstOrNull { badge ->
                        !userStreak.badges.contains(badge.id)  // User has not earned this badge yet
                    }

                    // If there is a next badge to achieve within this badge type, calculate progress towards it.
                    if (nextBadge != null) {
                        // Determine the current value relevant to this badge type from the user streak data.
                        // This current value represents how much progress the user has already made.
                        val currentValue = when (badgeType) {
                            BadgeType.STREAK -> userStreak.currentStreak
                            BadgeType.EXPENSE_COUNT -> userStreak.totalExpensesLogged
                            BadgeType.CATEGORY_DIVERSITY -> userStreak.categoriesUsed.size
                            BadgeType.EARLY_BIRD -> userStreak.earlyBirdCount
                            BadgeType.WEEKEND_WARRIOR -> userStreak.weekendLogCount
                            BadgeType.BUDGET_KEEPER -> userStreak.budgetKeeperDays
                        }

                        // Determine the target value required to earn the next badge.
                        // STREAK badges use a specific property (requiredStreak), others use a general requiredValue.
                        val requiredValue = when (badgeType) {
                            BadgeType.STREAK -> nextBadge.requiredStreak
                            else -> nextBadge.requiredValue
                        }

                        // Calculate progress as a ratio of current value to required value.
                        // The ratio is coerced to a maximum of 1.0 to indicate full completion or more.
                        progress[badgeType] = (currentValue.toFloat() / requiredValue).coerceAtMost(1f)
                    }
                }

                // Update the LiveData value with the latest progress map,
                // which will notify all observers of this new progress data.
                result.value = progress
            }
        }

        // Return the LiveData containing badge progress.
        // Observers will receive updates as the user streak changes.
        return result
    }


    // Get next badge for each category
    /**
     * Retrieves the next achievable badges for the current user, limited to the top three badges
     * that the user is closest to earning across all badge types.
     *
     * This function returns a LiveData that updates whenever the user's streak data changes,
     * providing a reactive stream of the next target badges for UI or other observers.
     */
    fun getNextBadges(): LiveData<List<Badge>> {
        // Get the ID of the currently logged-in user.
        // If no user is logged in (null), return an empty LiveData immediately,
        // since no badges can be fetched without a user context.
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())

        // Create a MutableLiveData to hold and emit the list of next badges.
        // Observers of this LiveData will get updates when the data changes.
        val result = MutableLiveData<List<Badge>>()

        // Observe the user's streak data (which holds progress and earned badges).
        // The observeForever method attaches a permanent observer that listens for changes
        // to the user streak data and triggers the lambda each time it updates.
        getUserStreak().observeForever { userStreak ->
            // Proceed only if the user's streak data is not null.
            if (userStreak != null) {
                // Retrieve the full list of all badges available in the system.
                val allBadges = Badge.getAllBadges()

                // Extract the list of badge IDs that the user has already earned from their streak.
                val earnedBadgeIds = userStreak.badges

                // For each badge type, find the next badge the user has not yet earned.
                // This is done by:
                // 1. Filtering badges to include only those of the current badgeType,
                //    and excluding badges the user already has.
                // 2. Selecting the badge with the smallest required value (closest target)
                //    for that badge type.
                val nextBadges = BadgeType.values().mapNotNull { badgeType ->
                    allBadges
                        .filter { badge ->
                            badge.badgeType == badgeType && !earnedBadgeIds.contains(badge.id)
                        }
                        .minByOrNull { badge ->
                            // Determine which requirement to use for comparison depending on badge type.
                            // For STREAK badges, use requiredStreak property,
                            // for all others use requiredValue property.
                            when (badgeType) {
                                BadgeType.STREAK -> badge.requiredStreak
                                else -> badge.requiredValue
                            }
                        }
                }
                    // After finding one "next" badge per badge type,
                    // take only the top 3 badges closest to completion to limit the list size.
                    .take(3)

                // Update the LiveData with the list of next badges,
                // notifying any observers that new data is available.
                result.value = nextBadges
            }
        }

        // Return the LiveData containing the next badges list.
        // This allows any UI or other component to observe and react to badge progress changes.
        return result
    }


    // Check if user can earn any badge right now
    /**
     * Checks immediately if the current user has earned any new badges that
     * they qualify for based on their current streak and progress.
     *
     * This function runs asynchronously within the ViewModel's coroutine scope,
     * ensuring that it does not block the main thread while fetching user data
     * and evaluating badge criteria.
     *
     * If new badges are found, it logs the count for further handling,
     * such as triggering UI notifications or updates.
     */
    fun checkForImmediateBadges() = viewModelScope.launch {
        // Retrieve the ID of the currently logged-in user.
        // If there is no user logged in (null), terminate early from the coroutine.
        val currentUserId = getCurrentUserId() ?: return@launch

        // Fetch the current user's streak data from the repository.
        // If the user streak is not available (null), terminate early as
        // there is no progress data to check against badge requirements.
        val userStreak = repository.getUserStreak(currentUserId) ?: return@launch

        // Call the repository function that checks for all new badges the user
        // has just earned but not yet been awarded, based on the current streak.
        val newBadges = repository.checkForAllNewBadges(userStreak)

        // If there are any new badges earned, perform an action such as logging
        // or triggering a badge notification/update in the UI.
        if (newBadges.isNotEmpty()) {
            // Log a debug message indicating how many new badges are available
            // for the user to claim or be notified about.
            android.util.Log.d("ViewModels", "User can immediately earn ${newBadges.size} badges!")

            // Note: This is where additional logic could be added to update the UI,
            // notify the user, or update the user streak with the newly earned badges.
        }
    }


    // Get detailed statistics for gamification screen
    /**
     * Retrieves detailed gamification statistics for the current user as a LiveData object.
     *
     * This function observes the user's streak data and maps it into a comprehensive
     * GamificationStats object that summarizes various progress metrics, points, badges,
     * and achievements. The LiveData result updates automatically whenever the user's
     * streak data changes, enabling UI components to reactively display up-to-date stats.
     */
    fun getDetailedStats(): LiveData<GamificationStats> {
        // Attempt to get the currently logged-in user's ID.
        // If no user is logged in (null), immediately return a MutableLiveData
        // with an empty/default GamificationStats instance to avoid null values downstream.
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(GamificationStats())

        // Create a MutableLiveData instance that will hold the detailed stats to be observed.
        val result = MutableLiveData<GamificationStats>()

        // Observe the user's streak LiveData continuously with observeForever,
        // meaning this observer remains active and listens for any changes to the user's streak data.
        getUserStreak().observeForever { userStreak ->
            // Only proceed if user streak data is not null (i.e., available).
            if (userStreak != null) {
                // Map the user's streak data into a GamificationStats object with detailed properties.
                // Each property reflects key gamification metrics such as streak counts, points, badges, and milestones.
                val stats = GamificationStats(
                    currentStreak = userStreak.currentStreak,              // The user's current consecutive active days.
                    longestStreak = userStreak.longestStreak,              // The user's longest streak ever achieved.
                    totalPoints = userStreak.points,                        // Total points accumulated by the user.
                    totalExpenses = userStreak.totalExpensesLogged,        // Total number of expenses logged by the user.
                    categoriesUsed = userStreak.categoriesUsed.size,       // Number of unique categories used in expenses.
                    earlyBirdCount = userStreak.earlyBirdCount,            // Count of days where expenses were logged early (early bird).
                    weekendCount = userStreak.weekendLogCount,             // Count of expenses logged on weekends.
                    budgetKeeperDays = userStreak.budgetKeeperDays,        // Number of days the user stayed within budget.
                    perfectWeeks = userStreak.perfectWeeks,                 // Number of "perfect" weeks meeting all goals.
                    streakLevel = userStreak.getStreakLevel(),              // Derived level based on the current streak.
                    progressToNext = userStreak.getProgressToNextMilestone(), // Progress as a float towards the next milestone.
                    nextMilestone = userStreak.getNextStreakMilestone(),    // The target milestone the user is currently working toward.
                    badges = userStreak.badges,                              // List of badge IDs the user has earned.
                    achievements = userStreak.achievements                   // List of other achievements unlocked by the user.
                )

                // Update the LiveData's value with the newly constructed stats object,
                // notifying any observers (such as UI components) of the new data.
                result.value = stats
            }
        }

        // Return the LiveData object that observers can subscribe to for detailed gamification stats.
        return result
    }


    // Update budget keeper status when budget is checked
    fun updateBudgetStatus(stayedUnderBudget: Boolean) = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch
        repository.updateBudgetKeeperStatus(currentUserId, stayedUnderBudget)
    }


// ------------------------ Search and Filter Methods ------------------------

    /**
     * Search and filter expenses with advanced criteria
     */
    fun searchAndFilterExpenses(filter: ExpenseFilter): LiveData<List<Expense>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        return repository.searchAndFilterExpenses(currentUserId, filter)
    }

    /**
     * Get search suggestions based on user input
     */
    fun getSearchSuggestions(query: String): LiveData<List<String>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        val result = MutableLiveData<List<String>>()

        viewModelScope.launch {
            try {
                val suggestions = repository.getSearchSuggestions(currentUserId, query)
                result.value = suggestions
            } catch (e: Exception) {
                android.util.Log.e("ViewModels", "Error getting search suggestions", e)
                result.value = emptyList()
            }
        }

        return result
    }

    /**
     * Get popular search terms for quick access
     */
    fun getPopularSearchTerms(): LiveData<List<String>> {
        val currentUserId = getCurrentUserId() ?: return MutableLiveData(emptyList())
        val result = MutableLiveData<List<String>>()

        viewModelScope.launch {
            try {
                val terms = repository.getPopularSearchTerms(currentUserId)
                result.value = terms
            } catch (e: Exception) {
                android.util.Log.e("ViewModels", "Error getting popular search terms", e)
                result.value = emptyList()
            }
        }

        return result
    }

    /**
     * Save user search query for analytics/suggestions
     */
    fun saveSearchQuery(query: String) = viewModelScope.launch {
        // This could be used to track popular searches
        // For now, we'll just log it
        android.util.Log.d("ViewModels", "User searched for: $query")
    }

    /**
     * Get expense statistics for current filter
     */
    fun getExpenseStatistics(filter: ExpenseFilter): LiveData<ExpenseStatistics> {
        val result = MediatorLiveData<ExpenseStatistics>()

        val expensesLiveData = searchAndFilterExpenses(filter)

        result.addSource(expensesLiveData) { expenses ->
            if (expenses.isNotEmpty()) {
                val statistics = calculateExpenseStatistics(expenses)
                result.value = statistics
            } else {
                result.value = ExpenseStatistics()
            }
        }

        return result
    }

    /**
     * Calculate statistics from expense list
     * Calculates various statistical summaries from a list of Expense objects.
     *
     * This function processes the given list of expenses to compute key financial metrics
     * such as total amount spent, average expense amount, maximum and minimum expense,
     * breakdown of spending by category, and the date range covered by the expenses.
     */
    private fun calculateExpenseStatistics(expenses: List<Expense>): ExpenseStatistics {
        // If the list of expenses is empty, return a default/empty ExpenseStatistics object immediately.
        // This prevents further computation on empty data and avoids errors.
        if (expenses.isEmpty()) return ExpenseStatistics()

        // Calculate the total amount spent across all expenses by summing their totalAmount fields.
        val totalAmount = expenses.sumOf { it.totalAmount }

        // Calculate the average expense amount by dividing totalAmount by the number of expenses.
        // This gives an overall sense of typical spending per transaction.
        val averageAmount = totalAmount / expenses.size

        // Find the maximum expense amount among all expenses.
        // Use maxOfOrNull to safely handle empty lists, defaulting to 0.0 if no expenses exist.
        val maxAmount = expenses.maxOfOrNull { it.totalAmount } ?: 0.0

        // Find the minimum expense amount among all expenses.
        // Similar to maxAmount, safely defaulting to 0.0 if no data.
        val minAmount = expenses.minOfOrNull { it.totalAmount } ?: 0.0

        // Compute a breakdown of total spending grouped by category.
        // Steps:
        // - Group expenses by their 'category' property, resulting in a map from category to list of expenses.
        // - For each category, sum the totalAmount of its expenses.
        // - Convert the map to a list of pairs (category, totalAmount).
        // - Sort this list in descending order by total amount to identify highest spending categories first.
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { (_, categoryExpenses) ->
                categoryExpenses.sumOf { it.totalAmount }
            }
            .toList()
            .sortedByDescending { it.second }

        // Extract all expense dates as Date objects by calling getExpenseDateAsDate() on each expense.
        val dates = expenses.map { it.getExpenseDateAsDate() }

        // Find the earliest expense date, representing the start of the date range.
        val startDate = dates.minOrNull()

        // Find the latest expense date, representing the end of the date range.
        val endDate = dates.maxOrNull()

        // Construct and return an ExpenseStatistics object populated with all the calculated metrics:
        return ExpenseStatistics(
            totalAmount = totalAmount,                        // Total amount spent.
            averageAmount = averageAmount,                    // Average expense amount.
            maxAmount = maxAmount,                            // Highest single expense amount.
            minAmount = minAmount,                            // Lowest single expense amount.
            transactionCount = expenses.size,                 // Total number of expense entries.
            categoryBreakdown = categoryTotals,               // List of categories with their total spending.
            dateRange = Pair(startDate, endDate),             // Date range covered by the expenses (start and end).
            topCategory = categoryTotals.firstOrNull()?.first ?: "",       // Category with the highest spending, or empty string if none.
            topCategoryAmount = categoryTotals.firstOrNull()?.second ?: 0.0 // Amount spent in the top category, or 0.0 if none.
        )
    }


    /**
     * Data class for expense statistics
     */
    data class ExpenseStatistics(
        val totalAmount: Double = 0.0,
        val averageAmount: Double = 0.0,
        val maxAmount: Double = 0.0,
        val minAmount: Double = 0.0,
        val transactionCount: Int = 0,
        val categoryBreakdown: List<Pair<String, Double>> = emptyList(),
        val dateRange: Pair<Date?, Date?> = Pair(null, null),
        val topCategory: String = "",
        val topCategoryAmount: Double = 0.0
    )

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

    fun createExpenseWithSeparatePhotoUpload(
        categoryId: String?,
        categoryName: String,
        expenseDate: Date,
        startTime: String?,
        endTime: String?,
        description: String,
        amount: Double,
        photoPath: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            onError("User not authenticated")
            return@launch
        }

        try {
            android.util.Log.d("ViewModels", "=== CREATING EXPENSE WITH SEPARATE PHOTO UPLOAD ===")
            android.util.Log.d("ViewModels", "User: $currentUserId, Amount: $amount, Photo: ${photoPath != null}")

            // Create expense WITHOUT photo first (this ensures gamification always works)
            val expense = Expense(
                userId = currentUserId,
                categoryId = categoryId,
                category = categoryName,
                expenseDate = Timestamp(expenseDate),
                startTime = startTime,
                endTime = endTime,
                description = description,
                totalAmount = amount,
                photoUrl = null, // Will be updated later if photo upload succeeds
                photoPath = photoPath
            )

            android.util.Log.d("ViewModels", "Step 1: Inserting expense into Firestore")
            val expenseId = repository.insertExpense(expense)
            android.util.Log.d("ViewModels", "Step 1: Expense created with ID: $expenseId")

            // Update gamification IMMEDIATELY after expense creation
            android.util.Log.d("ViewModels", "Step 2: Updating gamification streak")
            updateStreakOnExpenseLog(expenseDate, categoryId)
            android.util.Log.d("ViewModels", "Step 2: Gamification update completed")

            // Handle photo upload separately (if fails, expense still exists)
            if (!photoPath.isNullOrEmpty()) {
                android.util.Log.d("ViewModels", "Step 3: Starting photo upload")

                // Launch photo upload in separate coroutine so it doesn't block
                launch {
                    try {
                        val photoUrl = repository.uploadPhoto(photoPath, currentUserId)
                        if (photoUrl != null) {
                            android.util.Log.d("ViewModels", "Step 3a: Photo uploaded successfully: $photoUrl")

                            // Update expense with photo URL
                            val updatedExpense = expense.copy(
                                id = expenseId,
                                photoUrl = photoUrl
                            )
                            repository.updateExpense(updatedExpense)
                            android.util.Log.d("ViewModels", "Step 3b: Expense updated with photo URL")
                        } else {
                            android.util.Log.w("ViewModels", "Step 3: Photo upload failed, but expense was still created")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ViewModels", "Step 3: Photo upload error (expense still exists): ${e.message}", e)
                        // Don't call onError here since the expense was created successfully
                    }
                }
            } else {
                android.util.Log.d("ViewModels", "Step 3: No photo to upload")
            }

            // Return success immediately (don't wait for photo upload)
            android.util.Log.d("ViewModels", "=== EXPENSE CREATION COMPLETED SUCCESSFULLY ===")
            onSuccess()

        } catch (e: Exception) {
            android.util.Log.e("ViewModels", "Error in createExpenseWithSeparatePhotoUpload", e)
            onError(e.message ?: "Unknown error occurred")
        }
    }


    // Debug method to check current streak status
    fun debugStreakStatus() = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch

        try {
            android.util.Log.d("ViewModels", "=== DEBUG STREAK STATUS ===")

            val streak = repository.getUserStreak(currentUserId)
            if (streak != null) {
                android.util.Log.d("ViewModels", "Current streak: ${streak.currentStreak}")
                android.util.Log.d("ViewModels", "Total expenses: ${streak.totalExpensesLogged}")
                android.util.Log.d("ViewModels", "Points: ${streak.points}")
                android.util.Log.d("ViewModels", "Badges: ${streak.badges}")
                android.util.Log.d("ViewModels", "Last log date: ${streak.getLastLogDateAsDate()}")
            } else {
                android.util.Log.w("ViewModels", "No streak found for user")
            }
        } catch (e: Exception) {
            android.util.Log.e("ViewModels", "Error debugging streak: ${e.message}", e)
        }
    }

    // Force streak refresh
    fun forceStreakRefresh() = viewModelScope.launch {
        val currentUserId = getCurrentUserId() ?: return@launch

        try {
            // Re-initialize if needed
            initializeUserStreak()

            // Debug current state
            debugStreakStatus()

        } catch (e: Exception) {
            android.util.Log.e("ViewModels", "Error forcing streak refresh: ${e.message}", e)
        }
    }

}