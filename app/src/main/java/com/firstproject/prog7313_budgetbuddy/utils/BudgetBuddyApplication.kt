package com.firstproject.prog7313_budgetbuddy.utils

import android.app.Application
import com.firstproject.prog7313_budgetbuddy.utils.ThemeManager

class BudgetBuddyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize theme
        val themeManager = ThemeManager.getInstance(this)
        themeManager.initializeTheme()
    }
}