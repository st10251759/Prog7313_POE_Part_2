package com.firstproject.prog7313_budgetbuddy

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


// Imports required Android and Room database libraries and classes.
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.firstproject.prog7313_budgetbuddy.data.Converters
import com.firstproject.prog7313_budgetbuddy.data.dao.*
import com.firstproject.prog7313_budgetbuddy.data.entities.*

// Imports custom type converters and DAOs (Data Access Objects).
@Database(
    entities = [
        Category::class,
        Expense::class,
        Photo::class,
        BudgetGoal::class
    ],
    version = 2,  // Increment version number due to schema change
    exportSchema = false
)
// Converts custom types (e.g., Date) to Room-compatible types using the specified converters class.
@TypeConverters(Converters::class)
abstract class BudgetBuddyDatabase : RoomDatabase() {

    // Abstract function that provides access to Category table operations (CRUD).
    abstract fun categoryDao(): CategoryDao
    // Abstract function that provides access to Expense table operations.
    abstract fun expenseDao(): ExpenseDao
    // Abstract function that provides access to Photo table operations.
    abstract fun photoDao(): PhotoDao
    // Abstract function that provides access to BudgetGoal table operations.
    abstract fun budgetGoalDao(): BudgetGoalDao

    /**
     * Singleton pattern to ensure only one instance of the database exists
     * throughout the app’s lifecycle. Prevents memory leaks and ensures consistency.
     */

    companion object {
        @Volatile
        private var INSTANCE: BudgetBuddyDatabase? = null


        /**
         * Returns the singleton instance of the database.
         * If it's already created, return it.
         * If not, create a new instance using Room’s database builder.
         */
        fun getDatabase(context: Context): BudgetBuddyDatabase {
            // First, check if an instance already exists.
            return INSTANCE ?: synchronized(this) {
                // If not, create one in a thread-safe way.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetBuddyDatabase::class.java,
                    "budget_buddy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance  // Save this instance for reuse.
                instance              // Return the newly created instance.
            }
        }
    }
}
