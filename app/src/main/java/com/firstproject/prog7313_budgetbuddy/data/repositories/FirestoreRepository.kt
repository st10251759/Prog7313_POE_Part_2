package com.firstproject.prog7313_budgetbuddy.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firstproject.prog7313_budgetbuddy.data.models.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "FirestoreRepository"
        private const val CATEGORIES_COLLECTION = "categories"
        private const val EXPENSES_COLLECTION = "expenses"
        private const val BUDGET_GOALS_COLLECTION = "budget_goals"
        private const val PHOTOS_STORAGE_PATH = "expense_photos"
        private const val USER_STREAKS_COLLECTION = "user_streaks"
    }

    // ------------------------ Category Repository Methods ------------------------

    suspend fun insertCategory(category: Category): String {
        return try {
            val docRef = db.collection(CATEGORIES_COLLECTION).document()
            val categoryWithId = category.copy(id = docRef.id)
            docRef.set(categoryWithId.toMap()).await()
            Log.d(TAG, "Category inserted successfully with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting category: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateCategory(category: Category) {
        try {
            db.collection(CATEGORIES_COLLECTION)
                .document(category.id)
                .set(category.toMap())
                .await()
            Log.d(TAG, "Category updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteCategory(category: Category) {
        try {
            db.collection(CATEGORIES_COLLECTION)
                .document(category.id)
                .delete()
                .await()
            Log.d(TAG, "Category deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category: ${e.message}", e)
            throw e
        }
    }

    fun getAllCategoriesByUser(userId: String): LiveData<List<Category>> {
        val result = MutableLiveData<List<Category>>()

        Log.d(TAG, "Setting up categories listener for user: $userId")

        db.collection(CATEGORIES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting categories: ${error.message}", error)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "Categories snapshot is null")
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                val categories = snapshot.documents.mapNotNull { doc ->
                    try {
                        val category = doc.toObject(Category::class.java)?.apply { id = doc.id }
                        Log.d(TAG, "Loaded category: ${category?.categoryName}")
                        category
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting category document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                Log.d(TAG, "Categories loaded: ${categories.size}")
                result.value = categories
            }

        return result
    }

    suspend fun getCategoryById(categoryId: String): Category? {
        return try {
            val doc = db.collection(CATEGORIES_COLLECTION)
                .document(categoryId)
                .get()
                .await()

            doc.toObject(Category::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category by ID: ${e.message}", e)
            null
        }
    }

    suspend fun getCategoryByName(name: String, userId: String): Category? {
        return try {
            val query = db.collection(CATEGORIES_COLLECTION)
                .whereEqualTo("categoryName", name)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            query.documents.firstOrNull()?.toObject(Category::class.java)?.apply {
                id = query.documents.first().id
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category by name: ${e.message}", e)
            null
        }
    }

    // ------------------------ Expense Repository Methods ------------------------

    suspend fun insertExpense(expense: Expense): String {
        return try {
            val docRef = db.collection(EXPENSES_COLLECTION).document()
            val expenseWithId = expense.copy(id = docRef.id)
            docRef.set(expenseWithId.toMap()).await()
            Log.d(TAG, "Expense inserted successfully with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateExpense(expense: Expense) {
        try {
            db.collection(EXPENSES_COLLECTION)
                .document(expense.id)
                .set(expense.toMap())
                .await()
            Log.d(TAG, "Expense updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        try {
            // Delete photo from storage if exists
            expense.photoUrl?.let { photoUrl ->
                try {
                    storage.getReferenceFromUrl(photoUrl).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete photo from storage: ${e.message}", e)
                }
            }

            // Delete expense document
            db.collection(EXPENSES_COLLECTION)
                .document(expense.id)
                .delete()
                .await()
            Log.d(TAG, "Expense deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting expense: ${e.message}", e)
            throw e
        }
    }

    fun getAllExpensesByUser(userId: String): LiveData<List<Expense>> {
        val result = MutableLiveData<List<Expense>>()

        Log.d(TAG, "Setting up expenses listener for user: $userId")

        // Simplified query without ordering to avoid index issues
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting expenses: ${error.message}", error)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "Expenses snapshot is null")
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        val expense = doc.toObject(Expense::class.java)?.apply { id = doc.id }
                        Log.d(TAG, "Loaded expense: ${expense?.description}")
                        expense
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting expense document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                // Sort expenses by date in memory (descending order - newest first)
                val sortedExpenses = expenses.sortedByDescending { it.expenseDate.toDate() }
                Log.d(TAG, "Expenses loaded and sorted: ${sortedExpenses.size}")
                result.value = sortedExpenses
            }

        return result
    }

    fun getExpensesByPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<Expense>> {
        val result = MutableLiveData<List<Expense>>()

        val startTimestamp = Timestamp(startDate)
        val endTimestamp = Timestamp(endDate)

        Log.d(TAG, "Setting up period expenses listener for user: $userId, from: $startDate to: $endDate")

        // Simplified query to avoid index issues
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting expenses by period: ${error.message}", error)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "Period expenses snapshot is null")
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Expense::class.java)?.apply { id = doc.id }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting expense document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                // Filter by date in memory and sort
                val filteredExpenses = expenses.filter { expense ->
                    val expenseTime = expense.expenseDate
                    expenseTime >= startTimestamp && expenseTime <= endTimestamp
                }.sortedByDescending { it.expenseDate.toDate() }

                Log.d(TAG, "Period expenses loaded and filtered: ${filteredExpenses.size}")
                result.value = filteredExpenses
            }

        return result
    }

    suspend fun getExpenseById(expenseId: String): Expense? {
        return try {
            val doc = db.collection(EXPENSES_COLLECTION)
                .document(expenseId)
                .get()
                .await()

            doc.toObject(Expense::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expense by ID: ${e.message}", e)
            null
        }
    }

    fun getExpensesByCategory(userId: String, categoryId: String): LiveData<List<Expense>> {
        val result = MutableLiveData<List<Expense>>()

        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting expenses by category: ${error.message}", error)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Expense::class.java)?.apply { id = doc.id }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting expense document ${doc.id}: ${e.message}", e)
                        null
                    }
                }.sortedByDescending { it.expenseDate.toDate() }

                result.value = expenses
            }

        return result
    }

    fun getTotalExpensesForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<Double> {
        val result = MutableLiveData<Double>()

        val startTimestamp = Timestamp(startDate)
        val endTimestamp = Timestamp(endDate)

        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting total expenses: ${error.message}", error)
                    result.value = 0.0
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    result.value = 0.0
                    return@addSnapshotListener
                }

                val total = snapshot.documents.sumOf { doc ->
                    try {
                        val expense = doc.toObject(Expense::class.java)
                        if (expense != null) {
                            val expenseTime = expense.expenseDate
                            if (expenseTime >= startTimestamp && expenseTime <= endTimestamp) {
                                expense.totalAmount
                            } else {
                                0.0
                            }
                        } else {
                            0.0
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting amount from document: ${e.message}", e)
                        0.0
                    }
                }

                result.value = total
            }

        return result
    }

    fun getCategorySpendingForPeriod(userId: String, startDate: Date, endDate: Date): LiveData<List<CategorySpending>> {
        val result = MutableLiveData<List<CategorySpending>>()

        val startTimestamp = Timestamp(startDate)
        val endTimestamp = Timestamp(endDate)

        // Get expenses and categories separately to avoid complex queries
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { expenseSnapshot, expenseError ->
                if (expenseError != null) {
                    Log.e(TAG, "Error getting expenses for category spending: ${expenseError.message}", expenseError)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Get categories
                db.collection(CATEGORIES_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { categorySnapshot ->
                        try {
                            val categories = categorySnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Category::class.java)?.apply { id = doc.id }
                            }

                            val expenses = expenseSnapshot?.documents?.mapNotNull { doc ->
                                doc.toObject(Expense::class.java)?.apply { id = doc.id }
                            } ?: emptyList()

                            // Filter expenses by date period
                            val periodExpenses = expenses.filter { expense ->
                                val expenseTime = expense.expenseDate
                                expenseTime >= startTimestamp && expenseTime <= endTimestamp
                            }

                            // Group expenses by category and calculate totals
                            val categorySpending = categories.mapNotNull { category ->
                                val categoryExpenses = periodExpenses.filter { it.categoryId == category.id }
                                if (categoryExpenses.isNotEmpty()) {
                                    val total = categoryExpenses.sumOf { it.totalAmount }
                                    CategorySpending(category, total)
                                } else null
                            }.sortedByDescending { it.amount }

                            result.value = categorySpending
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing category spending data: ${e.message}", e)
                            result.value = emptyList()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error getting categories for spending: ${e.message}", e)
                        result.value = emptyList()
                    }
            }

        return result
    }

    // ------------------------ Budget Goal Repository Methods ------------------------

    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): String {
        return try {
            val docRef = db.collection(BUDGET_GOALS_COLLECTION).document()
            val goalWithId = budgetGoal.copy(id = docRef.id)
            docRef.set(goalWithId.toMap()).await()
            Log.d(TAG, "Budget goal inserted successfully with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting budget goal: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal) {
        try {
            db.collection(BUDGET_GOALS_COLLECTION)
                .document(budgetGoal.id)
                .set(budgetGoal.toMap())
                .await()
            Log.d(TAG, "Budget goal updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating budget goal: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal) {
        try {
            db.collection(BUDGET_GOALS_COLLECTION)
                .document(budgetGoal.id)
                .delete()
                .await()
            Log.d(TAG, "Budget goal deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting budget goal: ${e.message}", e)
            throw e
        }
    }

    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?> {
        val result = MutableLiveData<BudgetGoal?>()

        // Simplified query without ordering
        db.collection(BUDGET_GOALS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting current budget goal: ${error.message}", error)
                    result.value = null
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    result.value = null
                    return@addSnapshotListener
                }

                // Get all budget goals and find the most recent one in memory
                val budgetGoals = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(BudgetGoal::class.java)?.apply { id = doc.id }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting budget goal document: ${e.message}", e)
                        null
                    }
                }

                val mostRecentGoal = budgetGoals.maxByOrNull { it.startDate.toDate() }
                result.value = mostRecentGoal
            }

        return result
    }

    suspend fun getBudgetGoalForDate(userId: String, date: Date): BudgetGoal? {
        return try {
            val timestamp = Timestamp(date)

            val query = db.collection(BUDGET_GOALS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Filter by date in memory to avoid complex queries
            query.documents.mapNotNull { doc ->
                doc.toObject(BudgetGoal::class.java)?.apply { id = doc.id }
            }.find { goal ->
                timestamp >= goal.startDate && timestamp <= goal.endDate
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting budget goal for date: ${e.message}", e)
            null
        }
    }

    suspend fun getBudgetGoalById(goalId: String): BudgetGoal? {
        return try {
            val doc = db.collection(BUDGET_GOALS_COLLECTION)
                .document(goalId)
                .get()
                .await()

            doc.toObject(BudgetGoal::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting budget goal by ID: ${e.message}", e)
            null
        }
    }

    // ------------------------ Photo Storage Methods ------------------------

    suspend fun uploadPhoto(photoPath: String, userId: String): String? {
        return try {
            val file = java.io.File(photoPath)
            if (!file.exists()) {
                Log.e(TAG, "Photo file does not exist: $photoPath")
                return null
            }

            val photoRef = storage.reference
                .child("$PHOTOS_STORAGE_PATH/$userId/${UUID.randomUUID()}.jpg")

            val uploadTask = photoRef.putFile(android.net.Uri.fromFile(file)).await()
            val downloadUrl = photoRef.downloadUrl.await()

            Log.d(TAG, "Photo uploaded successfully: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo: ${e.message}", e)
            null
        }
    }

    suspend fun deletePhoto(photoUrl: String) {
        try {
            storage.getReferenceFromUrl(photoUrl).delete().await()
            Log.d(TAG, "Photo deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting photo: ${e.message}", e)
        }
    }

    // ------------------------ Gamification Repository Methods ------------------------

    suspend fun getUserStreak(userId: String): UserStreak? {
        return try {
            Log.d(TAG, "Getting user streak for: $userId")
            val doc = db.collection(USER_STREAKS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                Log.d(TAG, "Found existing streak document")
                doc.toObject(UserStreak::class.java)?.apply { id = doc.id }
            } else {
                Log.d(TAG, "No streak document found, creating new one")
                // Create new streak record if doesn't exist
                val newStreak = UserStreak(
                    id = userId,
                    userId = userId,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastLogDate = Timestamp(Date(0)), // Set to epoch to ensure first log counts
                    totalDaysLogged = 0,
                    badges = emptyList(),
                    points = 0
                )
                createOrUpdateUserStreak(newStreak)
                newStreak
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user streak: ${e.message}", e)
            null
        }
    }

    suspend fun createOrUpdateUserStreak(userStreak: UserStreak) {
        try {
            Log.d(TAG, "Creating/updating user streak for: ${userStreak.userId}")
            db.collection(USER_STREAKS_COLLECTION)
                .document(userStreak.userId)
                .set(userStreak.toMap())
                .await()
            Log.d(TAG, "User streak updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user streak: ${e.message}", e)
            throw e
        }
    }

    fun observeUserStreak(userId: String): LiveData<UserStreak?> {
        val result = MutableLiveData<UserStreak?>()

        Log.d(TAG, "Setting up streak listener for user: $userId")

        db.collection(USER_STREAKS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing user streak: ${error.message}", error)
                    result.value = null
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Streak document updated")
                    val streak = snapshot.toObject(UserStreak::class.java)?.apply { id = snapshot.id }
                    result.value = streak
                } else {
                    Log.d(TAG, "No streak document found")
                    result.value = null
                }
            }

        return result
    }

    // Check if user has logged expense today
    suspend fun hasLoggedExpenseToday(userId: String): Boolean {
        return try {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val tomorrow = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, 1)
            }.time

            val query = db.collection(EXPENSES_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("expenseDate", Timestamp(today))
                .whereLessThan("expenseDate", Timestamp(tomorrow))
                .limit(1)
                .get()
                .await()

            !query.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking today's expenses: ${e.message}", e)
            false
        }
    }

    // **MOST IMPORTANT METHOD**: Update streak when expense is logged
    suspend fun updateUserStreakOnExpenseLog(userId: String): UserStreak? {
        return try {
            Log.d(TAG, "=== STARTING STREAK UPDATE FOR USER: $userId ===")

            val currentStreak = getUserStreak(userId) ?: return null
            Log.d(TAG, "Current streak data: currentStreak=${currentStreak.currentStreak}, longestStreak=${currentStreak.longestStreak}")

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val lastLogCalendar = Calendar.getInstance().apply {
                time = currentStreak.getLastLogDateAsDate()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val daysDifference = ((today.timeInMillis - lastLogCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            Log.d(TAG, "Days difference: $daysDifference")

            val updatedStreak = when {
                daysDifference == 0 -> {
                    Log.d(TAG, "Already logged today, no change")
                    // Already logged today, no change
                    currentStreak
                }
                daysDifference == 1 -> {
                    Log.d(TAG, "Consecutive day, incrementing streak")
                    // Consecutive day, increment streak
                    currentStreak.copy(
                        currentStreak = currentStreak.currentStreak + 1,
                        longestStreak = maxOf(currentStreak.longestStreak, currentStreak.currentStreak + 1),
                        lastLogDate = Timestamp.now(),
                        totalDaysLogged = currentStreak.totalDaysLogged + 1
                    )
                }
                else -> {
                    Log.d(TAG, "Streak broken, resetting to 1")
                    // Streak broken, reset to 1
                    currentStreak.copy(
                        currentStreak = 1,
                        lastLogDate = Timestamp.now(),
                        totalDaysLogged = currentStreak.totalDaysLogged + 1
                    )
                }
            }

            // Check for new badges
            val newBadges = checkForNewBadges(updatedStreak)
            Log.d(TAG, "New badges earned: ${newBadges.size}")

            val finalStreak = if (newBadges.isNotEmpty()) {
                val allBadges = (updatedStreak.badges + newBadges.map { it.id }).distinct()
                val bonusPoints = newBadges.sumOf { it.points }
                Log.d(TAG, "Adding ${newBadges.size} badges and $bonusPoints points")
                updatedStreak.copy(
                    badges = allBadges,
                    points = updatedStreak.points + bonusPoints
                )
            } else {
                updatedStreak
            }

            Log.d(TAG, "Final streak: currentStreak=${finalStreak.currentStreak}, longestStreak=${finalStreak.longestStreak}, points=${finalStreak.points}")

            createOrUpdateUserStreak(finalStreak)
            Log.d(TAG, "=== STREAK UPDATE COMPLETED ===")
            finalStreak
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user streak: ${e.message}", e)
            null
        }
    }

    private fun checkForNewBadges(userStreak: UserStreak): List<Badge> {
        val allBadges = Badge.getAllBadges()
        val earnedBadgeIds = userStreak.badges
        val newBadges = mutableListOf<Badge>()

        for (badge in allBadges) {
            if (!earnedBadgeIds.contains(badge.id) && userStreak.currentStreak >= badge.requiredStreak) {
                Log.d(TAG, "New badge earned: ${badge.name} (required: ${badge.requiredStreak}, current: ${userStreak.currentStreak})")
                newBadges.add(badge)
            }
        }

        return newBadges
    }
}