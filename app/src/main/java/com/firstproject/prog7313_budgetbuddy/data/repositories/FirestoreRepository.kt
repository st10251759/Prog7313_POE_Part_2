package com.firstproject.prog7313_budgetbuddy.data.repositories

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

 Title: Kotlin Coroutines: Suspend Functions
 Author: Guruprasad Hegde
 Date Published: 08 October 2024
 Date Accessed: 20 May 2025
 Code Version: N/A
 Availability: https://medium.com/@guruprasadhegde4/kotlin-coroutines-suspend-function-f98ebbbd3bd7

  --------------------------------Code Attribution----------------------------------
*/

//Imports
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
    // Initialize a Firestore database instance for reading and writing data
    private val db = FirebaseFirestore.getInstance()
    // Initialize a Firebase Storage instance for handling file uploads (e.g., images, documents)
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "FirestoreRepository"    // Tag used for logging purposes in the FirestoreRepository class
        private const val CATEGORIES_COLLECTION = "categories" // Firestore collection name for storing user-defined expense categories
        private const val EXPENSES_COLLECTION = "expenses"   // Firestore collection name for storing individual expense records
        private const val BUDGET_GOALS_COLLECTION = "budget_goals" // Firestore collection name for storing user budget goals
        private const val PHOTOS_STORAGE_PATH = "expense_photos" // Firebase Storage path for storing uploaded expense-related photos
        private const val USER_STREAKS_COLLECTION = "user_streaks" // Firestore collection name for storing user streak data for gamification
    }

    // ------------------------ Category Repository Methods ------------------------

    // Suspended function to insert a new category into Firestore
    suspend fun insertCategory(category: Category): String {
        return try {
            // Generate a new document reference with a unique ID in the "categories" collection
            val docRef = db.collection(CATEGORIES_COLLECTION).document()
            // Create a copy of the category object with the generated ID
            val categoryWithId = category.copy(id = docRef.id)
            // Save the category data to Firestore by converting it to a map
            docRef.set(categoryWithId.toMap()).await()
            // Log success message
            Log.d(TAG, "Category inserted successfully with ID: ${docRef.id}")
            // Return the generated document ID
            docRef.id
        } catch (e: Exception) {
            // Log the error if insertion fails and rethrow the exception
            Log.e(TAG, "Error inserting category: ${e.message}", e)
            throw e
        }
    }

    // Suspended function to update an existing category in Firestore
    suspend fun updateCategory(category: Category) {
        try {
            // Access the "categories" collection and target the document by its ID
            db.collection(CATEGORIES_COLLECTION)
                .document(category.id)
                // Overwrite the document with the updated category data
                .set(category.toMap())
                .await()

            // Log a success message if the update completes without error
            Log.d(TAG, "Category updated successfully")
        } catch (e: Exception) {
            // Log the error and rethrow it to allow calling code to handle the exception
            Log.e(TAG, "Error updating category: ${e.message}", e)
            throw e
        }
    }


// Deletes a specific category from the Firestore database.
// This function is a suspend function and should be called from a coroutine or another suspend function.
    suspend fun deleteCategory(category: Category) {
        try {
            // Access the Firestore "categories" collection and delete the document by its ID.
            db.collection(CATEGORIES_COLLECTION)
                .document(category.id)
                .delete()
                .await() // Suspends until the deletion is complete.

            // Log success message upon successful deletion.
            Log.d(TAG, "Category deleted successfully")
        } catch (e: Exception) {
            // Log any error that occurs during the deletion process and rethrow the exception.
            Log.e(TAG, "Error deleting category: ${e.message}", e)
            throw e
        }
    }


// Retrieves all categories from Firestore that belong to a specific user in real-time.
// Returns the data as LiveData so it can be observed from the UI.
    fun getAllCategoriesByUser(userId: String): LiveData<List<Category>> {
        // MutableLiveData to hold the result and allow updates to observers
        val result = MutableLiveData<List<Category>>()

        // Log the initialization of the Firestore listener
        Log.d(TAG, "Setting up categories listener for user: $userId")

        // Query the "categories" collection where userId matches the provided userId
        db.collection(CATEGORIES_COLLECTION)
            .whereEqualTo("userId", userId)
            // Set up a real-time listener to react to changes in the database
            .addSnapshotListener { snapshot, error ->
                // Handle Firestore query errors
                if (error != null) {
                    Log.e(TAG, "Error getting categories: ${error.message}", error)
                    result.value = emptyList() // Provide an empty list if error occurs
                    return@addSnapshotListener
                }

                // Handle the case where the snapshot is null (no data returned)
                if (snapshot == null) {
                    Log.w(TAG, "Categories snapshot is null")
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Map each Firestore document to a Category object
                val categories = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Convert document to Category and assign its document ID
                        val category = doc.toObject(Category::class.java)?.apply { id = doc.id }
                        Log.d(TAG, "Loaded category: ${category?.categoryName}")
                        category
                    } catch (e: Exception) {
                        // Log errors during conversion but continue processing others
                        Log.e(TAG, "Error converting category document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                // Log the number of categories loaded and update LiveData
                Log.d(TAG, "Categories loaded: ${categories.size}")
                result.value = categories
            }

        // Return the LiveData for observation
        return result
    }


    // Suspends execution to retrieve a single category from Firestore using its unique document ID.
// Returns a Category object if found, or null if an error occurs or the document doesn't exist.
    suspend fun getCategoryById(categoryId: String): Category? {
        return try {
            // Fetch the document with the given ID from the "categories" collection.
            val doc = db.collection(CATEGORIES_COLLECTION)
                .document(categoryId)
                .get()
                .await() // Await the result of the asynchronous Firestore call.

            // Convert the document snapshot into a Category object, assigning the document ID to the category.
            doc.toObject(Category::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            // Log any exceptions and return null in case of failure.
            Log.e(TAG, "Error getting category by ID: ${e.message}", e)
            null
        }
    }

    // Suspends execution to retrieve a category by name and userId.
// This ensures that categories are unique per user.
// Returns the first matching Category, or null if none found or an error occurs.
    suspend fun getCategoryByName(name: String, userId: String): Category? {
        return try {
            // Query the "categories" collection for documents matching both category name and userId.
            val query = db.collection(CATEGORIES_COLLECTION)
                .whereEqualTo("categoryName", name)
                .whereEqualTo("userId", userId)
                .limit(1) // Limit to the first match.
                .get()
                .await() // Await the query result.

            // Convert the first document (if any) to a Category object and assign the document ID.
            query.documents.firstOrNull()?.toObject(Category::class.java)?.apply {
                id = query.documents.first().id
            }
        } catch (e: Exception) {
            // Log any exceptions and return null on failure.
            Log.e(TAG, "Error getting category by name: ${e.message}", e)
            null
        }
    }


    // ------------------------ Expense Repository Methods ------------------------

    //Inserts a new expense into the Firestore database.
    suspend fun insertExpense(expense: Expense): String {
        return try {
            // Create a new document reference with an auto-generated ID in the expenses collection
            val docRef = db.collection(EXPENSES_COLLECTION).document()
            // Create a copy of the expense with the generated document ID assigned
            val expenseWithId = expense.copy(id = docRef.id)
            // Convert the expense object to a map and save it to Firestore asynchronously
            docRef.set(expenseWithId.toMap()).await()
            // Log success message with the generated document ID
            Log.d(TAG, "Expense inserted successfully with ID: ${docRef.id}")
            // Return the new document ID
            docRef.id
        } catch (e: Exception) {
            // Log error message and rethrow exception for the caller to handle
            Log.e(TAG, "Error inserting expense: ${e.message}", e)
            throw e
        }
    }

    //Updates an existing expense document in the Firestore database.
    suspend fun updateExpense(expense: Expense) {
        try {
            // Access the specific expense document by its ID and overwrite it with the updated data
            db.collection(EXPENSES_COLLECTION)
                .document(expense.id)
                .set(expense.toMap())
                .await()

            // Log success message after the update completes
            Log.d(TAG, "Expense updated successfully")
        } catch (e: Exception) {
            // Log error message and rethrow exception for the caller to handle
            Log.e(TAG, "Error updating expense: ${e.message}", e)
            throw e
        }
    }

    //Deletes an expense document from Firestore and its associated photo from Firebase Storage (if any).
    suspend fun deleteExpense(expense: Expense) {
        try {
            // Check if the expense has an associated photo URL
            expense.photoUrl?.let { photoUrl ->
                try {
                    // Attempt to delete the photo file from Firebase Storage using the URL
                    storage.getReferenceFromUrl(photoUrl).delete().await()
                } catch (e: Exception) {
                    // Log a warning if photo deletion fails, but continue with expense deletion
                    Log.w(TAG, "Could not delete photo from storage: ${e.message}", e)
                }
            }

            // Delete the expense document from the Firestore collection by document ID
            db.collection(EXPENSES_COLLECTION)
                .document(expense.id)
                .delete()
                .await()

            // Log success message after expense document deletion
            Log.d(TAG, "Expense deleted successfully")
        } catch (e: Exception) {
            // Log error message and rethrow exception if expense deletion fails
            Log.e(TAG, "Error deleting expense: ${e.message}", e)
            throw e
        }
    }

    //Retrieves all expenses for a given user and provides them as LiveData.
    //Sets up a real-time listener on the expenses collection filtered by userId.
    fun getAllExpensesByUser(userId: String): LiveData<List<Expense>> {
        // MutableLiveData to hold and emit the list of expenses
        val result = MutableLiveData<List<Expense>>()

        Log.d(TAG, "Setting up expenses listener for user: $userId")

        // Query Firestore for expenses where the 'userId' field matches the given userId
        // Using a simplified query without ordering to avoid Firestore index issues
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error and set empty list if there is a problem retrieving data
                    Log.e(TAG, "Error getting expenses: ${error.message}", error)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    // Log warning and set empty list if snapshot is unexpectedly null
                    Log.w(TAG, "Expenses snapshot is null")
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Convert each document snapshot to an Expense object
                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Map document data to Expense and assign Firestore document ID
                        val expense = doc.toObject(Expense::class.java)?.apply { id = doc.id }
                        Log.d(TAG, "Loaded expense: ${expense?.description}")
                        expense
                    } catch (e: Exception) {
                        // Log error for any document conversion failures and skip that document
                        Log.e(TAG, "Error converting expense document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                // Sort expenses by expenseDate descending (newest first) in-memory
                val sortedExpenses = expenses.sortedByDescending { it.expenseDate.toDate() }
                Log.d(TAG, "Expenses loaded and sorted: ${sortedExpenses.size}")

                // Post the sorted list to LiveData observers
                result.value = sortedExpenses
            }

        // Return the LiveData object which updates in real-time as data changes
        return result
    }

    //Retrieves expenses for a given user within a specified time period and returns them as LiveData.
    //A Firestore snapshot listener is used to automatically update the data when changes occur.
    fun getExpensesByPeriod(
        userId: String,
        startDate: Date,
        endDate: Date
    ): LiveData<List<Expense>> {

        // MutableLiveData to hold and observe the list of expenses
        val result = MutableLiveData<List<Expense>>()

        // Convert Java Date objects to Firebase Timestamps for comparison
        val startTimestamp = Timestamp(startDate)
        val endTimestamp = Timestamp(endDate)

        // Log the parameters and intent for easier debugging and tracing
        Log.d(
            TAG,
            "Setting up period expenses listener for user: $userId, from: $startDate to: $endDate"
        )

        /**
         * Query Firestore's 'expenses' collection for documents where the userId matches the given ID.
         * This is a simplified query without range filtering in Firestore to avoid the need for composite indexes.
         * The actual date filtering will be done in memory after the data is retrieved.
         */
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                // Handle errors that may occur while listening to the Firestore snapshot
                if (error != null) {
                    Log.e(TAG, "Error getting expenses by period: ${error.message}", error)
                    result.value = emptyList() // Set value to an empty list in case of error
                    return@addSnapshotListener
                }

                // If the snapshot is unexpectedly null, log a warning and return an empty list
                if (snapshot == null) {
                    Log.w(TAG, "Period expenses snapshot is null")
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Convert Firestore documents into Expense objects
                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Attempt to convert the document to an Expense object
                        // Also attach the Firestore document ID to the Expense
                        doc.toObject(Expense::class.java)?.apply { id = doc.id }
                    } catch (e: Exception) {
                        // If conversion fails, log the error and skip this document
                        Log.e(TAG, "Error converting expense document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                /**
                 * Perform in-memory filtering of expenses by checking if the expenseDate
                 * falls within the start and end timestamps (inclusive).
                 * Then, sort the results in descending order by date (most recent first).
                 */
                val filteredExpenses = expenses.filter { expense ->
                    val expenseTime = expense.expenseDate
                    expenseTime >= startTimestamp && expenseTime <= endTimestamp
                }.sortedByDescending { it.expenseDate.toDate() }

                // Log the number of filtered expenses and update LiveData value
                Log.d(TAG, "Period expenses loaded and filtered: ${filteredExpenses.size}")
                result.value = filteredExpenses
            }

        // Return LiveData which will update whenever Firestore data changes
        return result
    }


     // Retrieves a single Expense from Firestore by its unique document ID.
     // This is a suspend function that performs the operation asynchronously using coroutines.
    suspend fun getExpenseById(expenseId: String): Expense? {
        return try {
            // Attempt to retrieve the expense document from Firestore by ID
            val doc = db.collection(EXPENSES_COLLECTION)
                .document(expenseId)
                .get()
                .await() // Suspends coroutine until the get() operation is complete

            // Convert the document snapshot into an Expense object
            // Also, assign the document ID to the 'id' property of the object
            doc.toObject(Expense::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            // Log the error if anything goes wrong during the fetch or conversion
            Log.e(TAG, "Error getting expense by ID: ${e.message}", e)
            null // Return null to indicate failure or missing document
        }
    }



     // Retrieves a live list of expenses for a specific user and category from Firestore.
     // The results are wrapped in LiveData to allow real-time updates.
    fun getExpensesByCategory(userId: String, categoryId: String): LiveData<List<Expense>> {
        // Mutable container for observed data that the UI can observe in real time
        val result = MutableLiveData<List<Expense>>()

        // Query the Firestore 'EXPENSES_COLLECTION' for expenses matching the given userId and categoryId
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)       // Filter by user
            .whereEqualTo("categoryId", categoryId) // Filter by category
            .addSnapshotListener { snapshot, error ->
                // If there's an error while listening for snapshot updates, log it and return an empty list
                if (error != null) {
                    Log.e(TAG, "Error getting expenses by category: ${error.message}", error)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // If the snapshot is null (possibly due to connectivity or no data), return an empty list
                if (snapshot == null) {
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Convert each document into an Expense object, assigning its Firestore ID
                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Expense::class.java)?.apply { id = doc.id }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting expense document ${doc.id}: ${e.message}", e)
                        null // Skip any documents that failed conversion
                    }
                }.sortedByDescending { it.expenseDate.toDate() } // Sort by date (newest first)

                // Update the LiveData value with the resulting list of expenses
                result.value = expenses
            }

        // Return the LiveData object, which can be observed for real-time updates
        return result
    }



     // Retrieves the total amount of expenses for a specific user within a given date range.
     // The result is returned as LiveData<Double> to support reactive UI updates.
    fun getTotalExpensesForPeriod(
        userId: String,
        startDate: Date,
        endDate: Date
    ): LiveData<Double> {
        // LiveData container to hold the result of the total expense calculation
        val result = MutableLiveData<Double>()

        // Convert the provided dates to Firebase-compatible Timestamp objects
        val startTimestamp = Timestamp(startDate)
        val endTimestamp = Timestamp(endDate)

        // Query Firestore for all expenses associated with the given userId
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                // Handle query errors
                if (error != null) {
                    Log.e(TAG, "Error getting total expenses: ${error.message}", error)
                    result.value = 0.0 // Return 0 if there's an error
                    return@addSnapshotListener
                }

                // Handle null snapshot (e.g., no data or network issues)
                if (snapshot == null) {
                    result.value = 0.0
                    return@addSnapshotListener
                }

                // Sum all valid expenses that fall within the specified date range
                val total = snapshot.documents.sumOf { doc ->
                    try {
                        // Convert the document to an Expense object
                        val expense = doc.toObject(Expense::class.java)
                        if (expense != null) {
                            val expenseTime = expense.expenseDate
                            // Only include expenses within the specified date range
                            if (expenseTime >= startTimestamp && expenseTime <= endTimestamp) {
                                expense.totalAmount
                            } else {
                                0.0
                            }
                        } else {
                            0.0 // Skip null expense objects
                        }
                    } catch (e: Exception) {
                        // Log and skip documents that fail conversion or contain invalid data
                        Log.e(TAG, "Error getting amount from document: ${e.message}", e)
                        0.0
                    }
                }

                // Update the LiveData with the computed total
                result.value = total
            }

        // Return LiveData so observers can react to updates in real-time
        return result
    }

    /**
     * Retrieves category-based spending totals for a given user within a specified date range.
     * The results are returned as LiveData containing a list of CategorySpending objects,
     * where each entry represents a category and the total amount spent in that category.
     */
    fun getCategorySpendingForPeriod(
        userId: String,
        startDate: Date,
        endDate: Date
    ): LiveData<List<CategorySpending>> {
        // Mutable LiveData to hold the final result that will be observed in the UI
        val result = MutableLiveData<List<CategorySpending>>()

        // Convert date inputs into Firebase-compatible Timestamp objects
        val startTimestamp = Timestamp(startDate)
        val endTimestamp = Timestamp(endDate)

        // Fetch expenses for the given user
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { expenseSnapshot, expenseError ->
                // Handle Firestore errors related to expense retrieval
                if (expenseError != null) {
                    Log.e(
                        TAG,
                        "Error getting expenses for category spending: ${expenseError.message}",
                        expenseError
                    )
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Fetch categories for the same user
                db.collection(CATEGORIES_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { categorySnapshot ->
                        try {
                            // Parse the category documents into Category objects
                            val categories = categorySnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Category::class.java)?.apply { id = doc.id }
                            }

                            // Parse the expense documents into Expense objects
                            val expenses = expenseSnapshot?.documents?.mapNotNull { doc ->
                                doc.toObject(Expense::class.java)?.apply { id = doc.id }
                            } ?: emptyList()

                            // Filter expenses to include only those within the date range
                            val periodExpenses = expenses.filter { expense ->
                                val expenseTime = expense.expenseDate
                                expenseTime >= startTimestamp && expenseTime <= endTimestamp
                            }

                            // Group filtered expenses by category and calculate totals
                            val categorySpending = categories.mapNotNull { category ->
                                // Find expenses that belong to the current category
                                val categoryExpenses = periodExpenses.filter {
                                    it.categoryId == category.id
                                }

                                // Only include the category if it has expenses in the period
                                if (categoryExpenses.isNotEmpty()) {
                                    // Sum the total amount spent in this category
                                    val total = categoryExpenses.sumOf { it.totalAmount }

                                    // Create a CategorySpending object with the category and its total
                                    CategorySpending(category, total)
                                } else null
                            }
                                // Sort results by spending amount in descending order
                                .sortedByDescending { it.amount }

                            // Update the LiveData with the final list of category spendings
                            result.value = categorySpending
                        } catch (e: Exception) {
                            // Handle parsing or logic errors gracefully
                            Log.e(TAG, "Error processing category spending data: ${e.message}", e)
                            result.value = emptyList()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle category retrieval errors
                        Log.e(TAG, "Error getting categories for spending: ${e.message}", e)
                        result.value = emptyList()
                    }
            }

        // Return LiveData object to allow observers (like UI) to react to updates
        return result
    }


    // ------------------------ Budget Goal Repository Methods ------------------------

    /**
     * Suspended function to insert a new BudgetGoal into the Firestore database.
     */
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): String {
        return try {
            // Create a new document reference with an auto-generated ID
            val docRef = db.collection(BUDGET_GOALS_COLLECTION).document()

            // Create a new BudgetGoal object with the generated ID included
            val goalWithId = budgetGoal.copy(id = docRef.id)

            // Convert the BudgetGoal to a map and store it in Firestore
            docRef.set(goalWithId.toMap()).await()

            // Log success and return the generated document ID
            Log.d(TAG, "Budget goal inserted successfully with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            // Log any error that occurs and rethrow the exception to be handled by the caller
            Log.e(TAG, "Error inserting budget goal: ${e.message}", e)
            throw e
        }
    }


    /**
     * Suspended function to update an existing BudgetGoal in the Firestore database.
     */
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal) {
        try {
            // Attempt to update the Firestore document with the given budget goal ID
            // The `set()` method will overwrite the entire document with new data
            db.collection(BUDGET_GOALS_COLLECTION)
                .document(budgetGoal.id)          // Reference the document by its ID
                .set(budgetGoal.toMap())          // Convert the object to a map for Firestore compatibility
                .await()                          // Suspend the coroutine until the update is complete

            // Log success message after update
            Log.d(TAG, "Budget goal updated successfully")
        } catch (e: Exception) {
            // Log any exception that occurs during the update
            Log.e(TAG, "Error updating budget goal: ${e.message}", e)

            // Re-throw the exception so the caller can handle it appropriately
            throw e
        }
    }


    /**
     * Suspended function to delete a budget goal from the Firestore database.
     */
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal) {
        try {
            // Attempt to locate and delete the document from the Firestore collection
            db.collection(BUDGET_GOALS_COLLECTION) // Reference the collection that stores budget goals
                .document(budgetGoal.id)           // Identify the specific document using the goal's ID
                .delete()                          // Issue a delete operation on the document
                .await()                           // Suspend the coroutine until the deletion is complete

            // Log a message indicating successful deletion
            Log.d(TAG, "Budget goal deleted successfully")
        } catch (e: Exception) {
            // Log any error that occurs during the deletion process
            Log.e(TAG, "Error deleting budget goal: ${e.message}", e)

            // Re-throw the exception so it can be handled by the caller
            throw e
        }
    }


    /**
     * Retrieves the most recent budget goal for a specific user from Firestore.
     * The result is returned as LiveData<BudgetGoal?> to support reactive updates in the UI.
     */
    fun getCurrentBudgetGoal(userId: String): LiveData<BudgetGoal?> {
        // MutableLiveData to hold and emit the result reactively
        val result = MutableLiveData<BudgetGoal?>()

        // Query the budget goals collection for documents where userId matches
        db.collection(BUDGET_GOALS_COLLECTION)
            .whereEqualTo("userId", userId) // Filter budget goals by the given userId
            .addSnapshotListener { snapshot, error -> // Listen for real-time updates to the data
                // Handle Firestore errors (e.g. permission issues, network failure)
                if (error != null) {
                    Log.e(TAG, "Error getting current budget goal: ${error.message}", error)
                    result.value = null
                    return@addSnapshotListener
                }

                // Handle null snapshot (no data returned or connection issue)
                if (snapshot == null) {
                    result.value = null
                    return@addSnapshotListener
                }

                // Attempt to parse documents into BudgetGoal objects
                val budgetGoals = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Deserialize document to BudgetGoal and assign the Firestore doc ID
                        doc.toObject(BudgetGoal::class.java)?.apply { id = doc.id }
                    } catch (e: Exception) {
                        // Log any conversion/serialization errors
                        Log.e(TAG, "Error converting budget goal document: ${e.message}", e)
                        null
                    }
                }

                // Determine the most recent goal based on startDate
                val mostRecentGoal = budgetGoals.maxByOrNull { it.startDate.toDate() }

                // Emit the most recent budget goal via LiveData
                result.value = mostRecentGoal
            }

        // Return the LiveData so the UI can observe changes
        return result
    }


    /**
     * Retrieves a budget goal for a specific user that is active on a given date.
     * This function is a suspending function and uses Firestore + Kotlin coroutines.
     */
    suspend fun getBudgetGoalForDate(userId: String, date: Date): BudgetGoal? {
        return try {
            // Convert the given Java Date into a Firestore Timestamp
            val timestamp = Timestamp(date)

            // Query all budget goals for the given user
            val query = db.collection(BUDGET_GOALS_COLLECTION)
                .whereEqualTo("userId", userId) // Filter by userId only
                .get()
                .await() // Await the async result using coroutine

            // Parse the documents into BudgetGoal objects
            val goals = query.documents.mapNotNull { doc ->
                doc.toObject(BudgetGoal::class.java)?.apply { id = doc.id }
            }

            // Filter in memory to find a goal where the date is between startDate and endDate
            goals.find { goal ->
                timestamp >= goal.startDate && timestamp <= goal.endDate
            }
        } catch (e: Exception) {
            // Log any errors during query or parsing
            Log.e(TAG, "Error getting budget goal for date: ${e.message}", e)
            null // Return null on failure
        }
    }


    /**
     * Retrieves a single BudgetGoal document from Firestore by its unique ID.
     * This is a suspending function that performs an asynchronous network call using Kotlin coroutines.
     */
    suspend fun getBudgetGoalById(goalId: String): BudgetGoal? {
        return try {
            // Attempt to get the document snapshot from the Firestore collection by ID
            val doc = db.collection(BUDGET_GOALS_COLLECTION)
                .document(goalId)  // Specify the document by its ID
                .get()            // Initiate the get operation (async)
                .await()          // Suspend coroutine until the task completes

            // Convert the Firestore document snapshot to a BudgetGoal object
            // Also set the 'id' property of the BudgetGoal to the document ID for reference
            doc.toObject(BudgetGoal::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            // Log any exceptions that occur during the fetch or conversion process
            Log.e(TAG, "Error getting budget goal by ID: ${e.message}", e)
            // Return null to indicate failure or absence of the budget goal
            null
        }
    }


    // ------------------------ Photo Storage Methods ------------------------

    /**
     * Uploads a photo file from the local filesystem to Firebase Storage under a user-specific path,
     * then returns the public download URL of the uploaded photo.
     * This is a suspending function that uses Kotlin coroutines to perform asynchronous Firebase Storage operations.
     */
    suspend fun uploadPhoto(photoPath: String, userId: String): String? {
        return try {
            // Create a File object from the provided local photo path
            val file = java.io.File(photoPath)

            // Check if the file actually exists; if not, log an error and return null early
            if (!file.exists()) {
                Log.e(TAG, "Photo file does not exist: $photoPath")
                return null
            }

            // Generate a reference in Firebase Storage where the photo will be uploaded
            // The path is organized as: PHOTOS_STORAGE_PATH/userId/randomUUID.jpg
            val photoRef = storage.reference
                .child("$PHOTOS_STORAGE_PATH/$userId/${UUID.randomUUID()}.jpg")

            // Upload the photo file asynchronously to the Firebase Storage location
            val uploadTask = photoRef.putFile(android.net.Uri.fromFile(file)).await()

            // Once upload completes, get the download URL for the uploaded photo
            val downloadUrl = photoRef.downloadUrl.await()

            // Log success with the download URL for debugging or tracking
            Log.d(TAG, "Photo uploaded successfully: $downloadUrl")

            // Return the string representation of the download URL to the caller
            downloadUrl.toString()
        } catch (e: Exception) {
            // If any error occurs during the process, log the exception message and stack trace
            Log.e(TAG, "Error uploading photo: ${e.message}", e)

            // Return null to indicate the upload failed
            null
        }
    }


    /**
     * Deletes a photo from Firebase Storage using its download URL.
     * This is a suspending function that performs the delete operation asynchronously using coroutines.
     */
    suspend fun deletePhoto(photoUrl: String) {
        try {
            // Get a reference to the photo in Firebase Storage from the provided URL
            val photoRef = storage.getReferenceFromUrl(photoUrl)

            // Delete the file at the referenced location asynchronously and wait for completion
            photoRef.delete().await()

            // Log a message indicating successful deletion
            Log.d(TAG, "Photo deleted successfully")
        } catch (e: Exception) {
            // If an error occurs during deletion, log the error message and stack trace
            Log.e(TAG, "Error deleting photo: ${e.message}", e)
        }
    }


    // ------------------------ Enhanced Gamification Repository Methods ------------------------

    /**
     * Retrieves the UserStreak data for a given user from Firestore.
     * This function attempts to fetch the streak document for the specified userId.
     * If the document exists, it loads and returns the UserStreak object.
     * If no document is found, it creates a new UserStreak with default values,
     * immediately saves it to Firestore, and returns the new streak object.
     * Logging is included for debugging to trace the flow and data states.
     */
    suspend fun getUserStreak(userId: String): UserStreak? {
        return try {
            Log.d(TAG, "=== GETTING USER STREAK ===")
            Log.d(TAG, "User ID: $userId")

            // Attempt to get the streak document for the user from Firestore
            val doc = db.collection(USER_STREAKS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                // Document found: deserialize to UserStreak and log details
                Log.d(TAG, "Found existing streak document")
                val streak = doc.toObject(UserStreak::class.java)?.apply { id = doc.id }
                Log.d(TAG, "Loaded streak: ${streak?.currentStreak} days, ${streak?.totalExpensesLogged} expenses")
                streak
            } else {
                // No document found: create a new UserStreak with default initial values
                Log.d(TAG, "No streak document found, creating new one")

                val newStreak = UserStreak(
                    id = userId,
                    userId = userId,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastLogDate = Timestamp(Date(0)), // Epoch date to ensure first log counts
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

                // Save the newly created streak immediately to Firestore
                createOrUpdateUserStreak(newStreak)
                Log.d(TAG, "New streak created and saved")
                newStreak
            }
        } catch (e: Exception) {
            // Log any error that occurs and return null
            Log.e(TAG, "Error getting user streak: ${e.message}", e)
            null
        }
    }


    /**
     * Creates a new user streak document or updates an existing one in Firestore.
     * This function saves the given UserStreak object to the USER_STREAKS_COLLECTION
     * using Firestore's `set` method with `merge` option. This ensures that existing
     * documents are updated without overwriting unspecified fields, and new documents
     * are created if they don't exist.
     */
    suspend fun createOrUpdateUserStreak(userStreak: UserStreak) {
        try {
            Log.d(TAG, "=== CREATING/UPDATING USER STREAK ===")
            Log.d(TAG, "User: ${userStreak.userId}, Streak: ${userStreak.currentStreak}, Expenses: ${userStreak.totalExpensesLogged}")

            // Use set() with merge to update existing fields or create a new document if none exists
            db.collection(USER_STREAKS_COLLECTION)
                .document(userStreak.userId)
                .set(userStreak.toMap(), com.google.firebase.firestore.SetOptions.merge())
                .await()

            Log.d(TAG, "User streak saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user streak: ${e.message}", e)
            throw e
        }
    }



    /**
     * Sets up a real-time listener on the user’s streak document in Firestore.
     * This function returns a LiveData<UserStreak?> that updates automatically whenever
     * the user streak document changes. It listens to the document identified by userId
     * in the USER_STREAKS_COLLECTION and posts updates to the LiveData.
     * If an error occurs during listening, the LiveData value is set to null.
     * If the document does not exist, the LiveData value is also set to null.
     */
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


    // Check if user has logged expense today using calendar days, not 24-hour periods
    /**
     * Checks if the specified user has logged any expenses today.
     *
     * This function queries the expenses collection to find if there is at least one expense
     * document for the given user where the expenseDate falls within the current day (from
     * midnight today to just before midnight tomorrow).
     *
     * It constructs the start of today (00:00:00.000) and the start of tomorrow (00:00:00.000
     * of the next day) to create a date range and queries Firestore for expenses within that range.

     */
    suspend fun hasLoggedExpenseToday(userId: String): Boolean {
        return try {
            // Get today's date at midnight (start of the day)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            // Get tomorrow's date at midnight (start of the next day)
            val tomorrow = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, 1)
            }.time

            // Query expenses where expenseDate is >= today and < tomorrow for the user
            val query = db.collection(EXPENSES_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("expenseDate", Timestamp(today))
                .whereLessThan("expenseDate", Timestamp(tomorrow))
                .limit(1)  // limit to 1 result for efficiency
                .get()
                .await()

            // Return true if at least one expense is found, false otherwise
            !query.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking today's expenses: ${e.message}", e)
            false
        }
    }




    // Enhanced streak update logic with better error handling
    /**
     * Updates the user's streak information when they log an expense.
     *
     * This function retrieves the current streak record (or creates it if missing),
     * calculates the day difference between the new expense date and the last logged date,
     * updates the streak counters and gamification statistics accordingly,
     * checks for new badges to award, and saves the updated streak back to the database.
     */
    suspend fun updateUserStreakOnExpenseLog(userId: String, expenseDate: Date, categoryId: String?): UserStreak? {
        return try {
            Log.d(TAG, "=== ENHANCED STREAK UPDATE START ===")
            Log.d(TAG, "User: $userId, Expense Date: $expenseDate, Category: $categoryId")

            // Retrieve or create the current user streak record
            var currentStreak = getUserStreak(userId)
            if (currentStreak == null) {
                Log.e(TAG, "Failed to get or create user streak")
                return null
            }
            Log.d(TAG, "Current streak loaded: ${currentStreak.currentStreak} days, ${currentStreak.totalExpensesLogged} expenses")

            // Normalize dates to midnight and calculate difference in days between
            // the new expense date and the last logged expense date in the streak
            val expenseCalendar = Calendar.getInstance().apply {
                time = expenseDate
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
            val daysDifference = ((expenseCalendar.timeInMillis - lastLogCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            Log.d(TAG, "Calendar days difference: $daysDifference")

            // Calculate gamification flags for the new expense:
            // - isEarlyBird: logged before 9 AM
            // - isWeekend: logged on Saturday or Sunday
            val expenseHour = Calendar.getInstance().apply { time = expenseDate }.get(Calendar.HOUR_OF_DAY)
            val isEarlyBird = expenseHour < 9
            val isWeekend = Calendar.getInstance().apply { time = expenseDate }.get(Calendar.DAY_OF_WEEK) in listOf(Calendar.SATURDAY, Calendar.SUNDAY)

            // Update categories used if the new expense's category is not already tracked
            val newCategoriesUsed = if (categoryId != null && !currentStreak.categoriesUsed.contains(categoryId)) {
                currentStreak.categoriesUsed + categoryId
            } else {
                currentStreak.categoriesUsed
            }
            Log.d(TAG, "Gamification stats: isEarlyBird=$isEarlyBird, isWeekend=$isWeekend, newCategories=${newCategoriesUsed.size}")

            // Update streak state based on daysDifference:
            // - 0 days: same day logged, update stats only
            // - 1 day: consecutive day, increment streak counters
            // - negative days: expense logged in the past, update stats only
            // - >1 day: streak broken, reset current streak to 1
            val updatedStreak = when {
                daysDifference == 0 -> {
                    Log.d(TAG, "Same day log - updating stats only")
                    currentStreak.copy(
                        totalExpensesLogged = currentStreak.totalExpensesLogged + 1,
                        categoriesUsed = newCategoriesUsed,
                        earlyBirdCount = if (isEarlyBird) currentStreak.earlyBirdCount + 1 else currentStreak.earlyBirdCount,
                        weekendLogCount = if (isWeekend) currentStreak.weekendLogCount + 1 else currentStreak.weekendLogCount
                    )
                }
                daysDifference == 1 -> {
                    Log.d(TAG, "Consecutive day - incrementing streak")
                    val newStreakCount = currentStreak.currentStreak + 1
                    currentStreak.copy(
                        currentStreak = newStreakCount,
                        longestStreak = maxOf(currentStreak.longestStreak, newStreakCount),
                        lastLogDate = Timestamp(expenseDate),
                        totalDaysLogged = currentStreak.totalDaysLogged + 1,
                        totalExpensesLogged = currentStreak.totalExpensesLogged + 1,
                        categoriesUsed = newCategoriesUsed,
                        earlyBirdCount = if (isEarlyBird) currentStreak.earlyBirdCount + 1 else currentStreak.earlyBirdCount,
                        weekendLogCount = if (isWeekend) currentStreak.weekendLogCount + 1 else currentStreak.weekendLogCount
                    )
                }
                daysDifference < 0 -> {
                    Log.d(TAG, "Expense in the past - updating stats only")
                    currentStreak.copy(
                        totalExpensesLogged = currentStreak.totalExpensesLogged + 1,
                        categoriesUsed = newCategoriesUsed,
                        earlyBirdCount = if (isEarlyBird) currentStreak.earlyBirdCount + 1 else currentStreak.earlyBirdCount,
                        weekendLogCount = if (isWeekend) currentStreak.weekendLogCount + 1 else currentStreak.weekendLogCount
                    )
                }
                else -> {
                    Log.d(TAG, "Streak broken - resetting to 1")
                    currentStreak.copy(
                        currentStreak = 1,
                        lastLogDate = Timestamp(expenseDate),
                        lastStreakBreak = Timestamp.now(),
                        totalDaysLogged = currentStreak.totalDaysLogged + 1,
                        totalExpensesLogged = currentStreak.totalExpensesLogged + 1,
                        categoriesUsed = newCategoriesUsed,
                        earlyBirdCount = if (isEarlyBird) currentStreak.earlyBirdCount + 1 else currentStreak.earlyBirdCount,
                        weekendLogCount = if (isWeekend) currentStreak.weekendLogCount + 1 else currentStreak.weekendLogCount
                    )
                }
            }

            // Check if any new badges should be awarded based on updated streak
            val newBadges = checkForAllNewBadges(updatedStreak)
            Log.d(TAG, "New badges to award: ${newBadges.map { it.name }}")

            // If badges earned, update badges list, points, and achievements accordingly
            val finalStreak = if (newBadges.isNotEmpty()) {
                val allBadges = (updatedStreak.badges + newBadges.map { it.id }).distinct()
                val bonusPoints = newBadges.sumOf { it.points }

                Log.d(TAG, "Awarding ${newBadges.size} badges worth $bonusPoints points")

                val newAchievements = if (newBadges.size > 1) {
                    updatedStreak.achievements + "multi_badge_${System.currentTimeMillis()}"
                } else {
                    updatedStreak.achievements
                }

                updatedStreak.copy(
                    badges = allBadges,
                    points = updatedStreak.points + bonusPoints,
                    achievements = newAchievements
                )
            } else {
                updatedStreak
            }

            Log.d(TAG, "Final streak: ${finalStreak.currentStreak} days, ${finalStreak.points} points, ${finalStreak.badges.size} badges")

            // Save the final updated streak to the database
            createOrUpdateUserStreak(finalStreak)
            Log.d(TAG, "=== ENHANCED STREAK UPDATE COMPLETE ===")

            finalStreak
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user streak: ${e.message}", e)
            null
        }
    }


    // More accurate badge checking with better validation
    /**
     * Checks which new badges the user has earned based on their current streak data.
     */
    fun checkForAllNewBadges(userStreak: UserStreak): List<Badge> {
        // Retrieve the complete list of all available badges
        val allBadges = Badge.getAllBadges()

        // Get the IDs of badges the user has already earned to avoid duplicates
        val earnedBadgeIds = userStreak.badges

        // Prepare a list to collect badges newly earned during this check
        val newBadges = mutableListOf<Badge>()

        // Log current user streak info for debugging
        Log.d(TAG, "Checking badges for user with streak: ${userStreak.currentStreak}, expenses: ${userStreak.totalExpensesLogged}")

        // Iterate over all badges to check eligibility
        for (badge in allBadges) {
            // Only check badges the user hasn't earned yet
            if (!earnedBadgeIds.contains(badge.id)) {
                // Determine if the user meets the criteria for the badge based on badge type
                val isEarned = when (badge.badgeType) {
                    BadgeType.STREAK -> {
                        // Check if user's current streak meets or exceeds the required streak length
                        val earned = userStreak.currentStreak >= badge.requiredStreak
                        Log.d(TAG, "Streak badge ${badge.id}: current=${userStreak.currentStreak}, required=${badge.requiredStreak}, earned=$earned")
                        earned
                    }
                    BadgeType.EXPENSE_COUNT -> {
                        // Check if total expenses logged meets required value
                        val earned = userStreak.totalExpensesLogged >= badge.requiredValue
                        Log.d(TAG, "Expense badge ${badge.id}: current=${userStreak.totalExpensesLogged}, required=${badge.requiredValue}, earned=$earned")
                        earned
                    }
                    BadgeType.CATEGORY_DIVERSITY -> {
                        // Check if number of distinct categories used meets required value
                        val earned = userStreak.categoriesUsed.size >= badge.requiredValue
                        Log.d(TAG, "Category badge ${badge.id}: current=${userStreak.categoriesUsed.size}, required=${badge.requiredValue}, earned=$earned")
                        earned
                    }
                    BadgeType.EARLY_BIRD -> {
                        // Check if early bird logs meet required value
                        val earned = userStreak.earlyBirdCount >= badge.requiredValue
                        Log.d(TAG, "Early bird badge ${badge.id}: current=${userStreak.earlyBirdCount}, required=${badge.requiredValue}, earned=$earned")
                        earned
                    }
                    BadgeType.WEEKEND_WARRIOR -> {
                        // Check if weekend logs meet required value
                        val earned = userStreak.weekendLogCount >= badge.requiredValue
                        Log.d(TAG, "Weekend badge ${badge.id}: current=${userStreak.weekendLogCount}, required=${badge.requiredValue}, earned=$earned")
                        earned
                    }
                    BadgeType.BUDGET_KEEPER -> {
                        // Check if budget keeper days meet required value
                        val earned = userStreak.budgetKeeperDays >= badge.requiredValue
                        Log.d(TAG, "Budget badge ${badge.id}: current=${userStreak.budgetKeeperDays}, required=${badge.requiredValue}, earned=$earned")
                        earned
                    }
                }

                // If badge criteria met, add it to the new badges list and log the achievement
                if (isEarned) {
                    Log.i(TAG, "🏆 NEW BADGE EARNED: ${badge.name} (${badge.badgeType}, ${badge.points} points)")
                    newBadges.add(badge)
                }
            }
        }

        // Log the total count of new badges earned after checking all badges
        Log.d(TAG, "Badge check complete: ${newBadges.size} new badges earned")

        // Return the list of newly earned badges
        return newBadges
    }


    // Method to update budget keeper status
    /**
     * Updates the user's budget keeper status by incrementing the count of days
     * the user stayed under budget and handles awarding any related badges.
     */
    suspend fun updateBudgetKeeperStatus(userId: String, stayedUnderBudget: Boolean) {
        try {
            // Retrieve the current user streak data; exit early if not found
            val currentStreak = getUserStreak(userId) ?: return

            // Only proceed if the user stayed under budget
            if (stayedUnderBudget) {
                // Create a new UserStreak object with incremented budgetKeeperDays count
                val updatedStreak = currentStreak.copy(
                    budgetKeeperDays = currentStreak.budgetKeeperDays + 1
                )

                // Check if the updated streak qualifies the user for any new badges
                val newBadges = checkForAllNewBadges(updatedStreak)

                // If new badges are earned, update the badges list and points total accordingly
                val finalStreak = if (newBadges.isNotEmpty()) {
                    val allBadges = (updatedStreak.badges + newBadges.map { it.id }).distinct()
                    val bonusPoints = newBadges.sumOf { it.points }

                    // Return a new UserStreak instance with updated badges and points
                    updatedStreak.copy(
                        badges = allBadges,
                        points = updatedStreak.points + bonusPoints
                    )
                } else {
                    // If no new badges, return the updated streak as is
                    updatedStreak
                }

                // Save the updated user streak data persistently
                createOrUpdateUserStreak(finalStreak)
            }
        } catch (e: Exception) {
            // Log any exceptions that occur during the update process
            Log.e(TAG, "Error updating budget keeper status: ${e.message}", e)
        }
    }



    /**
     * Search and filter expenses with advanced criteria
     * Sets up a real-time listener to search and filter expenses for a given user.
     */
    fun searchAndFilterExpenses(
        userId: String,
        filter: ExpenseFilter
    ): LiveData<List<Expense>> {
        // MutableLiveData to hold the filtered expense list and notify observers on changes
        val result = MutableLiveData<List<Expense>>()

        Log.d(TAG, "Setting up search/filter listener for user: $userId")

        // Start a Firestore query to get expenses matching the userId
        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                // Handle possible errors during query
                if (error != null) {
                    Log.e(TAG, "Error searching expenses: ${error.message}", error)
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Handle the case where snapshot is null
                if (snapshot == null) {
                    Log.w(TAG, "Search expenses snapshot is null")
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                // Convert Firestore documents to Expense objects, safely handling conversion errors
                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Expense::class.java)?.apply { id = doc.id }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting expense document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                // Apply in-memory filtering using the provided filter object (supports complex criteria)
                val filteredExpenses = applyFiltersToExpenses(expenses, filter)
                Log.d(TAG, "Filtered expenses: ${filteredExpenses.size} from ${expenses.size}")

                // Update LiveData with the filtered list, notifying observers
                result.value = filteredExpenses
            }

        // Return LiveData that updates automatically with filtered expenses
        return result
    }


    /**
     * Apply filters to expense list in memory
     */
    private fun applyFiltersToExpenses(expenses: List<Expense>, filter: ExpenseFilter): List<Expense> {
        var filteredExpenses = expenses

        // Apply keyword search
        if (filter.searchKeyword.isNotBlank()) {
            val keyword = filter.searchKeyword.lowercase()
            filteredExpenses = filteredExpenses.filter { expense ->
                expense.description.lowercase().contains(keyword) ||
                        expense.category.lowercase().contains(keyword)
            }
        }

        // Apply date range filter
        val (startDate, endDate) = filter.getDateRange()
        if (startDate != null || endDate != null) {
            filteredExpenses = filteredExpenses.filter { expense ->
                val expenseDate = expense.getExpenseDateAsDate()
                val afterStart = startDate?.let { expenseDate >= it } ?: true
                val beforeEnd = endDate?.let { expenseDate <= it } ?: true
                afterStart && beforeEnd
            }
        }

        // Apply amount range filter
        if (filter.minAmount != null || filter.maxAmount != null) {
            filteredExpenses = filteredExpenses.filter { expense ->
                val aboveMin = filter.minAmount?.let { expense.totalAmount >= it } ?: true
                val belowMax = filter.maxAmount?.let { expense.totalAmount <= it } ?: true
                aboveMin && belowMax
            }
        }

        // Apply category filter
        if (filter.selectedCategories.isNotEmpty()) {
            filteredExpenses = filteredExpenses.filter { expense ->
                expense.categoryId in filter.selectedCategories
            }
        }

        // Apply sorting
        filteredExpenses = when (filter.sortBy) {
            SortOption.DATE_DESC -> filteredExpenses.sortedByDescending { it.getExpenseDateAsDate() }
            SortOption.DATE_ASC -> filteredExpenses.sortedBy { it.getExpenseDateAsDate() }
            SortOption.AMOUNT_DESC -> filteredExpenses.sortedByDescending { it.totalAmount }
            SortOption.AMOUNT_ASC -> filteredExpenses.sortedBy { it.totalAmount }
            SortOption.CATEGORY -> filteredExpenses.sortedBy { it.category }
            SortOption.DESCRIPTION -> filteredExpenses.sortedBy { it.description }
        }

        return filteredExpenses
    }

    /**
     * Get quick search suggestions based on user's expense history
     */
    suspend fun getSearchSuggestions(userId: String, query: String): List<String> {
        return try {
            val snapshot = db.collection(EXPENSES_COLLECTION)
                .whereEqualTo("userId", userId)
                .limit(100) // Limit for performance
                .get()
                .await()

            val suggestions = mutableSetOf<String>()
            val queryLower = query.lowercase()

            snapshot.documents.forEach { doc ->
                val expense = doc.toObject(Expense::class.java)
                expense?.let {
                    // Add matching descriptions
                    if (it.description.lowercase().contains(queryLower)) {
                        suggestions.add(it.description)
                    }
                    // Add matching categories
                    if (it.category.lowercase().contains(queryLower)) {
                        suggestions.add(it.category)
                    }
                }
            }

            suggestions.take(5).toList() // Return top 5 suggestions
        } catch (e: Exception) {
            Log.e(TAG, "Error getting search suggestions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get popular search terms for the user
     */
    suspend fun getPopularSearchTerms(userId: String): List<String> {
        return try {
            val snapshot = db.collection(EXPENSES_COLLECTION)
                .whereEqualTo("userId", userId)
                .limit(50)
                .get()
                .await()

            val termFrequency = mutableMapOf<String, Int>()

            snapshot.documents.forEach { doc ->
                val expense = doc.toObject(Expense::class.java)
                expense?.let {
                    // Extract words from description
                    it.description.split("\\s+".toRegex()).forEach { word ->
                        if (word.length > 3) { // Only consider words longer than 3 characters
                            val cleanWord = word.lowercase().replace(Regex("[^a-z0-9]"), "")
                            if (cleanWord.isNotBlank()) {
                                termFrequency[cleanWord] = termFrequency.getOrDefault(cleanWord, 0) + 1
                            }
                        }
                    }
                }
            }

            // Return top 10 most frequent terms
            termFrequency.toList()
                .sortedByDescending { it.second }
                .take(10)
                .map { it.first }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting popular search terms: ${e.message}", e)
            emptyList()
        }
    }
}