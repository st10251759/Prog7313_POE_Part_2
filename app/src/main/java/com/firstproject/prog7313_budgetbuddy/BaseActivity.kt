package com.firstproject.prog7313_budgetbuddy

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
 Title: How to implement Dark mode(theme) Android studio | Kotlin
 Author: Mohsen Mashkour
 Date Published: 14 March 2023
 Date Accessed: 25 May 2025
 Code Version: N/A
 Availability: https://www.youtube.com/watch?v=AHsggyb0vGw

  --------------------------------Code Attribution----------------------------------
*/

import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.firstproject.prog7313_budgetbuddy.utils.ThemeManager

abstract class BaseActivity : AppCompatActivity() {

    // Late-initialized ThemeManager instance to manage app themes (light/dark)
    protected lateinit var themeManager: ThemeManager

    // Lifecycle method called when the activity comes to the foreground
    override fun onResume() {
        super.onResume()
        // Initialize themeManager if it hasn't been initialized yet
        if (!::themeManager.isInitialized) {
            themeManager = ThemeManager.getInstance(this)
        }
    }

    /**
     * Sets up a theme toggle button that switches between light and dark modes.
     * @param themeToggleButton The ImageButton used to toggle the theme.
     */
    protected fun setupThemeToggle(themeToggleButton: ImageButton) {
        // Ensure themeManager is initialized before use
        if (!::themeManager.isInitialized) {
            themeManager = ThemeManager.getInstance(this)
        }
        // Update the toggle button icon based on the current theme
        updateThemeIcon(themeToggleButton)

        // Set click listener to toggle theme on button press
        themeToggleButton.setOnClickListener {
            themeManager.toggleTheme()
            // Recreate activity to immediately apply the new theme changes
            recreate()
        }
    }

    /**
     * Updates the theme toggle button icon and accessibility content description
     * depending on whether night mode is active.
     * @param themeToggleButton The ImageButton to update.
     */
    private fun updateThemeIcon(themeToggleButton: ImageButton) {
        if (themeManager.isNightMode) {
            // If in night mode, show lightbulb icon to indicate switching to light mode
            themeToggleButton.setImageResource(R.drawable.ic_lightbulb)
            themeToggleButton.contentDescription = "Switch to light mode"
        } else {
            // If in light mode, show moon icon to indicate switching to night mode
            themeToggleButton.setImageResource(R.drawable.ic_moon)
            themeToggleButton.contentDescription = "Switch to night mode"
        }
    }
}
