package com.firstproject.prog7313_budgetbuddy

import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.firstproject.prog7313_budgetbuddy.utils.ThemeManager

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var themeManager: ThemeManager

    override fun onResume() {
        super.onResume()
        if (!::themeManager.isInitialized) {
            themeManager = ThemeManager.getInstance(this)
        }
    }

    protected fun setupThemeToggle(themeToggleButton: ImageButton) {
        if (!::themeManager.isInitialized) {
            themeManager = ThemeManager.getInstance(this)
        }
        updateThemeIcon(themeToggleButton)

        themeToggleButton.setOnClickListener {
            themeManager.toggleTheme()
            // Recreate activity to apply new theme
            recreate()
        }
    }

    private fun updateThemeIcon(themeToggleButton: ImageButton) {
        if (themeManager.isNightMode) {
            themeToggleButton.setImageResource(R.drawable.ic_lightbulb)
            themeToggleButton.contentDescription = "Switch to light mode"
        } else {
            themeToggleButton.setImageResource(R.drawable.ic_moon)
            themeToggleButton.contentDescription = "Switch to night mode"
        }
    }
}