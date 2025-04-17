package com.firstproject.prog7313_budgetbuddy

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.firstproject.prog7313_budgetbuddy.data.Converters
////import com.firstproject.prog7313_budgetbuddy.data.converters.BigDecimalConverter
//import com.firstproject.prog7313_budgetbuddy.data.converters.DateConverter
import com.firstproject.prog7313_budgetbuddy.data.dao.*
import com.firstproject.prog7313_budgetbuddy.data.entities.*

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
@TypeConverters(Converters::class)
abstract class BudgetBuddyDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun photoDao(): PhotoDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetBuddyDatabase? = null

        fun getDatabase(context: Context): BudgetBuddyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetBuddyDatabase::class.java,
                    "budget_buddy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
