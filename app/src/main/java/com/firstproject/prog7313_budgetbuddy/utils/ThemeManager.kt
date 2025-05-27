package com.firstproject.prog7313_budgetbuddy.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val THEME_PREFS = "theme_preferences"
        private const val NIGHT_MODE_KEY = "night_mode"

        @Volatile
        private var INSTANCE: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var isNightMode: Boolean
        get() = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false)
        set(value) {
            sharedPreferences.edit().putBoolean(NIGHT_MODE_KEY, value).apply()
            applyTheme(value)
        }

    fun applyTheme(isNightMode: Boolean) {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun toggleTheme() {
        isNightMode = !isNightMode
    }

    fun initializeTheme() {
        applyTheme(isNightMode)
    }
}