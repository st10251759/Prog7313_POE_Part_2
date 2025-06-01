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
 Title: Enum classes
 Author: Kotlin
 Date Published: 25 September 2024
 Date Accessed: 20 May 2025
 Code Version: v2.1.21
 Availability: https://kotlinlang.org/docs/enum-classes.html#
  --------------------------------Code Attribution----------------------------------
*/

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.adapters.BadgeAdapter
import com.firstproject.prog7313_budgetbuddy.data.models.Badge
import com.firstproject.prog7313_budgetbuddy.data.models.BadgeType
import com.firstproject.prog7313_budgetbuddy.data.models.GamificationStats
import com.firstproject.prog7313_budgetbuddy.data.models.UserStreak
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * GamificationActivity displays the user's streak information, badges earned, and progress
 * towards achievements. This activity motivates users to consistently log expenses by
 * gamifying the budget tracking experience with points, streaks, and rewards.
 */
class GamificationActivity : BaseActivity() {

    // Core components for data management and user authentication
    private lateinit var viewModel: ViewModels  // Manages gamification data and business logic
    private lateinit var auth: FirebaseAuth     // Handles user authentication state
    private lateinit var badgeAdapter: BadgeAdapter  // Manages badge display in RecyclerView

    // Main UI components for displaying streak and navigation
    private lateinit var btnBack: ImageButton           // Navigate back to previous screen
    private lateinit var btnThemeToggle: ImageButton    // Toggle between light/dark themes
    private lateinit var tvCurrentStreak: TextView      // Display current consecutive days streak
    private lateinit var tvLongestStreak: TextView      // Display user's all-time longest streak
    private lateinit var tvTotalPoints: TextView        // Show total gamification points earned
    private lateinit var tvStreakMessage: TextView      // Motivational message based on streak level
    private lateinit var progressBarNextBadge: ProgressBar  // Visual progress towards next badge
    private lateinit var tvNextBadgeName: TextView      // Name of the next badge to earn
    private lateinit var tvNextBadgeProgress: TextView  // Text showing progress (e.g., "5/7 days")
    private lateinit var rvBadges: RecyclerView         // Grid displaying all available badges
    private lateinit var tvLoggedToday: TextView        // Status of today's expense logging
    private lateinit var viewStreakIndicator: View      // Color indicator for today's logging status

    // **ENHANCED**: Additional UI components for detailed statistics display
    private lateinit var tvStreakLevel: TextView        // Show streak level (Newcomer, Champion, etc.)
    private lateinit var tvTotalExpenses: TextView      // Display total number of expenses logged
    private lateinit var tvCategoriesUsed: TextView     // Show number of different categories used
    private lateinit var tvEarlyBirdCount: TextView     // Count of morning expense logs
    private lateinit var tvWeekendCount: TextView       // Count of weekend expense logs
    private lateinit var progressBarOverall: ProgressBar  // Overall progress towards all achievements

    companion object {
        private const val TAG = "GamificationActivity"  // Log tag for debugging purposes
    }

    /**
     * Called when the activity is first created. Initializes all UI components,
     * sets up observers for data changes, and loads initial gamification data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable full-screen immersive experience
        enableEdgeToEdge()
        setContentView(R.layout.activity_gamification)

        // Handle system bar insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase authentication instance
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel for data management
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Set up all UI components and their references
        initializeUI()

        // Attach click listeners to interactive elements
        setupListeners()

        // Ensure proper initialization sequence - user streak must be initialized before loading UI
        initializeUserData()
    }

    /**
     * Initializes all UI components by finding their references and setting up
     * the RecyclerView with appropriate layout manager and adapter.
     */
    private fun initializeUI() {
        // Find and assign all basic UI component references
        btnBack = findViewById(R.id.btnBack)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak)
        tvLongestStreak = findViewById(R.id.tvLongestStreak)
        tvTotalPoints = findViewById(R.id.tvTotalPoints)
        tvStreakMessage = findViewById(R.id.tvStreakMessage)
        progressBarNextBadge = findViewById(R.id.progressBarNextBadge)
        tvNextBadgeName = findViewById(R.id.tvNextBadgeName)
        tvNextBadgeProgress = findViewById(R.id.tvNextBadgeProgress)
        rvBadges = findViewById(R.id.rvBadges)
        tvLoggedToday = findViewById(R.id.tvLoggedToday)
        viewStreakIndicator = findViewById(R.id.viewStreakIndicator)

        // Initialize additional enhanced UI components for detailed statistics
        tvStreakLevel = findViewById(R.id.tvStreakLevel)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvCategoriesUsed = findViewById(R.id.tvCategoriesUsed)
        tvEarlyBirdCount = findViewById(R.id.tvEarlyBirdCount)
        tvWeekendCount = findViewById(R.id.tvWeekendCount)
        progressBarOverall = findViewById(R.id.progressBarOverall)

        // Configure RecyclerView for badge display in a 3-column grid layout
        badgeAdapter = BadgeAdapter(emptyList(), emptyList())
        rvBadges.layoutManager = GridLayoutManager(this, 3)
        rvBadges.adapter = badgeAdapter

        // Setup theme toggle functionality if available from BaseActivity
        try {
            setupThemeToggle(btnThemeToggle)
        } catch (e: Exception) {
            // If theme toggle is not available, hide the button and log warning
            Log.w(TAG, "Theme toggle not available: ${e.message}")
            btnThemeToggle.visibility = View.GONE
        }
    }

    /**
     * Sets up click listeners for interactive UI elements including navigation,
     * detailed information display, and user engagement features.
     */
    private fun setupListeners() {
        // Handle back navigation
        btnBack.setOnClickListener {
            finish()
        }

        // **ENHANCED**: Show detailed streak information on long press for power users
        tvCurrentStreak.setOnLongClickListener {
            showDetailedStreakInfo()
            true
        }

        // **NEW**: Display points breakdown when user taps on total points
        tvTotalPoints.setOnClickListener {
            showPointsBreakdown()
        }
    }

    /**
     * Proper initialization sequence to ensure data integrity.
     * This method ensures user streak data is initialized before setting up observers
     * and loading dependent data to prevent null pointer exceptions.
     */
    private fun initializeUserData() {
        Log.d(TAG, "=== INITIALIZING USER DATA ===")

        //  Initialize user streak data in the database if it doesn't exist
        viewModel.initializeUserStreak()

        //  Set up observers to react to data changes with proper error handling
        setupStreakObserver()

        //  Load additional statistical data after core data is ready
        loadDetailedStats()
    }

    /**
     * Sets up the main observer for user streak data changes. This observer handles
     * updating the UI whenever streak data is modified and includes comprehensive
     * error handling for robust operation.
     */
    private fun setupStreakObserver() {
        viewModel.getUserStreak().observe(this) { userStreak ->
            try {
                if (userStreak != null) {
                    // Log successful data loading for debugging
                    Log.d(TAG, "✅ User streak loaded: ${userStreak.currentStreak} days, ${userStreak.totalExpensesLogged} expenses")

                    // Update all UI components with new streak data
                    updateStreakDisplay(userStreak)
                    updateBadgeDisplay(userStreak)
                    checkTodayStatus(userStreak)

                    // **NEW**: Force UI refresh to ensure all components are updated
                    refreshUIComponents()
                } else {
                    // Handle case where no streak data exists (new user)
                    Log.d(TAG, "⚠️ User streak is null, showing defaults")
                    showDefaultState()
                }
            } catch (e: Exception) {
                // Comprehensive error handling to prevent crashes
                Log.e(TAG, "Error in streak observer: ${e.message}", e)
                showDefaultState()
            }
        }
    }

    /**
     * Forces a refresh of UI components, particularly the RecyclerView,
     * to ensure data changes are properly reflected in the display.
     */
    private fun refreshUIComponents() {
        try {
            // Notify RecyclerView adapter that data has changed
            rvBadges.adapter?.notifyDataSetChanged()

            // Force layout refresh to handle any size or position changes
            rvBadges.invalidate()

            Log.d(TAG, "UI components refreshed")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing UI: ${e.message}", e)
        }
    }

    /**
     * Shows proper default state for new users who haven't started logging expenses yet.
     * This method ensures new users see encouraging messages and clear next steps rather than
     * empty or confusing UI elements.
     */
    private fun showDefaultState() {
        // Set basic statistics to zero for new users
        tvCurrentStreak.text = "0"
        tvLongestStreak.text = "0"
        tvTotalPoints.text = "0 pts"
        tvStreakLevel.text = "Newcomer"
        tvStreakMessage.text = "Start logging expenses to build your streak! 🚀"
        tvLoggedToday.text = "No expenses logged yet"

        // Show detailed statistics as zero for new users with encouraging icons
        tvTotalExpenses.text = "📝 0 expenses logged"
        tvCategoriesUsed.text = "🏷️ 0 categories explored"
        tvEarlyBirdCount.text = "🌅 0 early bird logs"
        tvWeekendCount.text = "🎉 0 weekend entries"

        // Set progress bar to 0 to clearly show starting point
        progressBarOverall.progress = 0

        // Show first badge as next target with 0 progress to guide user action
        val allBadges = viewModel.getAllBadges()
        val firstBadge = allBadges.find { it.id == "first_log" } ?: allBadges.firstOrNull()

        if (firstBadge != null) {
            tvNextBadgeName.text = "Next: ${firstBadge.name}"
            progressBarNextBadge.progress = 0  // **CRITICAL**: Show 0 progress for clarity

            // Determine the requirement value based on badge type
            val requirement = if (firstBadge.badgeType == BadgeType.STREAK) {
                firstBadge.requiredStreak
            } else {
                firstBadge.requiredValue
            }

            val unit = getBadgeTypeUnit(firstBadge.badgeType)
            tvNextBadgeProgress.text = "0/$requirement $unit"
        } else {
            // Fallback display if no badges are configured in the system
            tvNextBadgeName.text = "Start your journey!"
            progressBarNextBadge.progress = 0
            tvNextBadgeProgress.text = "Log your first expense"
        }

        // Display all available badges as unearned to show what's possible
        badgeAdapter.updateBadges(allBadges, emptyList())

        // Set encouraging today status for new users
        tvLoggedToday.text = "Ready to log your first expense?"
        tvLoggedToday.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.asparagus))

        Log.d(TAG, "Showing default state for new user")
    }

    /**
     * Loads additional detailed statistics and next badge information by setting up
     * observers for these data sources. This method handles the secondary data that
     * enhances the gamification experience.
     */
    private fun loadDetailedStats() {
        // Observe detailed statistics for comprehensive user activity overview
        viewModel.getDetailedStats().observe(this) { stats ->
            updateDetailedStatsDisplay(stats)
        }

        // Observe next available badges to show progression opportunities
        viewModel.getNextBadges().observe(this) { nextBadges ->
            updateNextBadgeInfo(nextBadges)
        }
    }

    /**
     * Updates the main streak display elements with current user data including
     * streak counts, points, level, and motivational messaging. Includes visual
     * enhancements like animations for achievement celebration.
     */
    private fun updateStreakDisplay(userStreak: UserStreak) {
        try {
            Log.d(TAG, "Updating streak display with: streak=${userStreak.currentStreak}, points=${userStreak.points}")

            // Safe string conversion and validation to prevent display errors
            tvCurrentStreak.text = userStreak.currentStreak.toString()
            tvLongestStreak.text = userStreak.longestStreak.toString()
            tvTotalPoints.text = "${userStreak.points} pts"

            //  Show dynamic streak level based on current achievement
            val streakLevel = userStreak.getStreakLevel()
            tvStreakLevel.text = streakLevel

            // Display contextual and encouraging messages based on progress
            tvStreakMessage.text = getStreakMessage(userStreak.currentStreak)

            // Add visual celebration through number animation
            animateStreakNumber(userStreak.currentStreak)

            Log.d(TAG, "Streak display updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating streak display: ${e.message}", e)
        }
    }

    /**
     * Returns accurate and motivational streak messages based on current progress.
     * Messages are designed to encourage continued engagement and celebrate milestones.
     */
    private fun getStreakMessage(currentStreak: Int): String {
        return when (currentStreak) {
            0 -> "Ready to start your journey? Log your first expense! 🚀"
            1 -> "Great start! One day down, many more to go! 💪"
            in 2..6 -> {
                val remaining = 7 - currentStreak
                "You're on fire! Just $remaining more day${if(remaining == 1) "" else "s"} to Week Warrior! 🗡️"
            }
            7 -> "🎉 Week Warrior achieved! You're building a solid habit!"
            in 8..13 -> {
                val remaining = 14 - currentStreak
                "Fantastic consistency! $remaining more day${if(remaining == 1) "" else "s"} to Fortnight Champion! 👑"
            }
            14 -> "🏆 Fortnight Champion! Your dedication is impressive!"
            in 15..29 -> {
                val remaining = 30 - currentStreak
                "Amazing discipline! $remaining more day${if(remaining == 1) "" else "s"} to Monthly Master! 🌟"
            }
            30 -> "⭐ Monthly Master! You've built an incredible habit!"
            in 31..59 -> {
                val remaining = 60 - currentStreak
                "Legendary consistency! $remaining more day${if(remaining == 1) "" else "s"} to Streak Legend! 👑"
            }
            else -> "🔥👑 Streak Legend! You're an inspiration to us all!"
        }
    }

    /**
     * Animates the streak number display with scaling effects based on achievement level.
     * Higher streaks get more dramatic animations to celebrate the user's commitment.
     */
    private fun animateStreakNumber(streak: Int) {
        // Determine animation scale based on streak achievement level
        val scale = when {
            streak >= 60 -> 1.3f    // Legendary streaks get maximum celebration
            streak >= 30 -> 1.25f   // Monthly masters get strong recognition
            streak >= 14 -> 1.2f    // Fortnight champions get good recognition
            streak >= 7 -> 1.15f    // Week warriors get moderate recognition
            else -> 1.1f            // Early streaks get gentle encouragement
        }

        // Only animate if user has any streak to celebrate
        if (streak > 0) {
            tvCurrentStreak.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(300)
                .withEndAction {
                    // Return to normal size after celebration
                    tvCurrentStreak.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start()
                }
                .start()
        }
    }

    /**
     * Updates the detailed statistics display with comprehensive user activity data.
     * This provides users with deeper insights into their expense logging patterns and habits.
     */
    private fun updateDetailedStatsDisplay(stats: GamificationStats) {
        // Display various statistics with emoji icons for visual appeal
        tvTotalExpenses.text = "📝 ${stats.totalExpenses} expenses logged"
        tvCategoriesUsed.text = "🏷️ ${stats.categoriesUsed} categories explored"
        tvEarlyBirdCount.text = "🌅 ${stats.earlyBirdCount} early bird logs"
        tvWeekendCount.text = "🎉 ${stats.weekendCount} weekend entries"

        //  Calculate and display accurate progress percentage with bounds checking
        val progress = (stats.progressToNext * 100).toInt().coerceIn(0, 100)
        progressBarOverall.progress = progress

        Log.d(TAG, "Updated stats: expenses=${stats.totalExpenses}, categories=${stats.categoriesUsed}, progress=$progress%")
    }

    /**
     * Updates the badge display by showing all available badges and highlighting earned ones.
     * Also calculates and displays progress towards the next achievable badge.
     */
    private fun updateBadgeDisplay(userStreak: UserStreak) {
        try {
            val allBadges = viewModel.getAllBadges()
            val earnedBadges = userStreak.badges

            Log.d(TAG, "Updating badge display: ${earnedBadges.size}/${allBadges.size} badges earned")
            Log.d(TAG, "Earned badges: $earnedBadges")

            // Update the badge grid with current earn status
            badgeAdapter.updateBadges(allBadges, earnedBadges)

            //  Calculate and show progress for the closest achievable badge
            updateNextBadgeProgress(userStreak, allBadges, earnedBadges)

            Log.d(TAG, "Badge display updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating badge display: ${e.message}", e)
        }
    }

    /**
     * Calculates more accurate next badge progress by finding the badge with the
     * lowest requirement that hasn't been earned yet, rather than showing the highest progress.
     */
    private fun updateNextBadgeProgress(userStreak: UserStreak, allBadges: List<Badge>, earnedBadges: List<String>) {
        //  Find the NEXT badge to work towards (lowest requirement among unearned badges)
        var nextBadge: Badge? = null
        var lowestRequirement = Int.MAX_VALUE

        // Search for the most achievable badge that hasn't been earned
        for (badge in allBadges) {
            if (!earnedBadges.contains(badge.id)) {
                val requirement = if (badge.badgeType == BadgeType.STREAK) {
                    badge.requiredStreak
                } else {
                    badge.requiredValue
                }

                //  Prioritize the badge with lowest requirement (most achievable)
                if (requirement < lowestRequirement) {
                    lowestRequirement = requirement
                    nextBadge = badge
                }
            }
        }

        if (nextBadge != null) {
            //  Calculate actual progress towards the specific next badge
            val progress = calculateBadgeProgress(userStreak, nextBadge)
            displayNextBadgeInfo(nextBadge, userStreak, progress)

            Log.d(TAG, "Next badge: ${nextBadge.name}, actual progress: ${(progress * 100).toInt()}%")
        } else {
            // Handle case where all badges are earned or no badges exist
            if (earnedBadges.isNotEmpty()) {
                displayAllBadgesEarned()
            } else {
                // Show appropriate guidance for completely new users
                displayFirstBadgePrompt()
            }
        }
    }

    /**
     *  Shows appropriate guidance for new users who haven't made any progress yet.
     * This helps orient users and gives them a clear first goal to work towards.
     */
    private fun displayFirstBadgePrompt() {
        val firstBadge = viewModel.getAllBadges().find { it.id == "first_log" }

        if (firstBadge != null) {
            tvNextBadgeName.text = "Next: ${firstBadge.name}"
            progressBarNextBadge.progress = 0  // **FIX**: Show 0 progress for clarity
            tvNextBadgeProgress.text = "0/${firstBadge.requiredStreak} ${getBadgeTypeUnit(firstBadge.badgeType)}"

            Log.d(TAG, "Showing first badge prompt for new user")
        } else {
            // Fallback if the expected first badge doesn't exist in the system
            tvNextBadgeName.text = "Start logging expenses!"
            progressBarNextBadge.progress = 0
            tvNextBadgeProgress.text = "0/1 expenses"
        }
    }

    /**
     * Calculates accurate progress percentage for any badge type by comparing
     * current user achievements against the specific badge requirements.
     */
    private fun calculateBadgeProgress(userStreak: UserStreak, badge: Badge): Float {
        // Get current user value for the specific badge type
        val currentValue = when (badge.badgeType) {
            BadgeType.STREAK -> userStreak.currentStreak
            BadgeType.EXPENSE_COUNT -> userStreak.totalExpensesLogged
            BadgeType.CATEGORY_DIVERSITY -> userStreak.categoriesUsed.size
            BadgeType.EARLY_BIRD -> userStreak.earlyBirdCount
            BadgeType.WEEKEND_WARRIOR -> userStreak.weekendLogCount
            BadgeType.BUDGET_KEEPER -> userStreak.budgetKeeperDays
        }

        // Get required value based on badge type structure
        val requiredValue = if (badge.badgeType == BadgeType.STREAK) {
            badge.requiredStreak
        } else {
            badge.requiredValue
        }

        //  Ensure safe division and proper bounds (0-100% progress)
        return if (requiredValue > 0) {
            (currentValue.toFloat() / requiredValue).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /**
     * Displays information about the next badge to earn, including name, progress bar,
     * and detailed progress text showing current vs required values.
     */
    private fun displayNextBadgeInfo(badge: Badge, userStreak: UserStreak, progress: Float) {
        tvNextBadgeName.text = "Next: ${badge.name}"

        // Ensure progress is never negative and properly bounded for new users
        val progressPercentage = (progress * 100).toInt().coerceIn(0, 100)
        progressBarNextBadge.progress = progressPercentage

        // Get current achievement value for this badge type
        val currentValue = when (badge.badgeType) {
            BadgeType.STREAK -> userStreak.currentStreak
            BadgeType.EXPENSE_COUNT -> userStreak.totalExpensesLogged
            BadgeType.CATEGORY_DIVERSITY -> userStreak.categoriesUsed.size
            BadgeType.EARLY_BIRD -> userStreak.earlyBirdCount
            BadgeType.WEEKEND_WARRIOR -> userStreak.weekendLogCount
            BadgeType.BUDGET_KEEPER -> userStreak.budgetKeeperDays
        }

        // Get target value for comparison
        val requiredValue = if (badge.badgeType == BadgeType.STREAK) {
            badge.requiredStreak
        } else {
            badge.requiredValue
        }

        // Create descriptive progress text with appropriate units
        val unit = getBadgeTypeUnit(badge.badgeType)
        tvNextBadgeProgress.text = "$currentValue/$requiredValue $unit"

        Log.d(TAG, "Displaying badge: ${badge.name}, current: $currentValue, required: $requiredValue, progress: $progressPercentage%")
    }

    /**
     * Displays celebration message when user has earned all available badges.
     * This provides recognition for complete achievement and encourages continued engagement.
     */
    private fun displayAllBadgesEarned() {
        tvNextBadgeName.text = "All badges earned! 🎉"
        progressBarNextBadge.progress = 100
        tvNextBadgeProgress.text = "Congratulations!"

        Log.d(TAG, "User has earned all available badges!")
    }

    /**
     * Returns descriptive unit names for different badge types to make
     * progress displays more user-friendly and understandable.
     */
    private fun getBadgeTypeUnit(badgeType: BadgeType): String {
        return when (badgeType) {
            BadgeType.STREAK -> "consecutive days"
            BadgeType.EXPENSE_COUNT -> "total expenses"
            BadgeType.CATEGORY_DIVERSITY -> "unique categories"
            BadgeType.EARLY_BIRD -> "morning logs"
            BadgeType.WEEKEND_WARRIOR -> "weekend days"
            BadgeType.BUDGET_KEEPER -> "budget days"
        }
    }

    /**
     * Handles updates to next badge information. Currently logs available badges
     * but could be enhanced to show multiple upcoming badges or cycle through them.
     */
    private fun updateNextBadgeInfo(nextBadges: List<Badge>) {
        // **NEW**: Future enhancement opportunity - could show multiple upcoming badges
        if (nextBadges.isNotEmpty()) {
            Log.d(TAG, "Next badges available: ${nextBadges.size}")
            // Future enhancement: show multiple next badges in a carousel view
        }
    }

    /**
     * Checks today's logging status using calendar days rather than timestamps
     * to provide more accurate daily streak tracking that aligns with user expectations.
     */
    private fun checkTodayStatus(userStreak: UserStreak) {
        try {
            // Normalize last log date to start of day for accurate comparison
            val lastLogCalendar = Calendar.getInstance().apply {
                time = userStreak.getLastLogDateAsDate()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Normalize today's date to start of day
            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Calculate difference in whole days
            val daysDifference = TimeUnit.MILLISECONDS.toDays(
                todayCalendar.timeInMillis - lastLogCalendar.timeInMillis
            )

            Log.d(TAG, "Days since last log: $daysDifference")

            updateTodayStatusDisplay(daysDifference)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking today status: ${e.message}", e)
            showDefaultTodayStatus()
        }
    }

    /**
     * Updates the today status display with appropriate message and color coding
     * based on how many days have passed since the last expense log.
     */
    private fun updateTodayStatusDisplay(daysDifference: Long) {
        // Determine message, text color, and indicator color based on streak status
        val (message, colorRes, indicatorColorRes) = when {
            daysDifference == 0L -> {
                // User has logged today - streak is safe
                Triple(
                    "✅ Expense logged today - streak safe!",
                    R.color.olivine,
                    R.color.olivine
                )
            }
            daysDifference == 1L -> {
                // User missed today but can still continue streak
                Triple(
                    "⚠️ Log an expense today to continue your streak!",
                    R.color.gold,
                    R.color.gold
                )
            }
            else -> {
                // Streak is broken - encourage fresh start
                Triple(
                    "❌ Streak broken - log an expense to start fresh!",
                    R.color.coral_pink,
                    R.color.coral_pink
                )
            }
        }

        // Apply the determined styling to UI components
        tvLoggedToday.text = message
        tvLoggedToday.setTextColor(ContextCompat.getColor(this, colorRes))
        viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, indicatorColorRes))
    }

    /**
     * Shows default today status for new users or when status cannot be determined.
     * Uses encouraging messaging and positive colors to motivate action.
     */
    private fun showDefaultTodayStatus() {
        tvLoggedToday.text = "Ready to log your first expense?"
        tvLoggedToday.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.asparagus))
    }

    /**
     * Displays comprehensive streak and activity statistics in a detailed format.
     * This power-user feature provides deep insights into their gamification progress.
     */
    private fun showDetailedStreakInfo() {
        viewModel.getUserStreak().value?.let { userStreak ->
            // Format comprehensive statistics message
            val message = """
               🔥 STREAK DETAILS
               
               Current Streak: ${userStreak.currentStreak} days
               Longest Streak: ${userStreak.longestStreak} days
               Level: ${userStreak.getStreakLevel()}
               Total Days Logged: ${userStreak.totalDaysLogged}
               
               📊 ACTIVITY STATS
               Total Expenses: ${userStreak.totalExpensesLogged}
               Categories Used: ${userStreak.categoriesUsed.size}
               Early Bird Logs: ${userStreak.earlyBirdCount}
               Weekend Logs: ${userStreak.weekendLogCount}
               Budget Keeper Days: ${userStreak.budgetKeeperDays}
               
               🏆 ACHIEVEMENT PROGRESS
               Points Earned: ${userStreak.points}
               Badges Earned: ${userStreak.badges.size}
               
               Next Milestone: ${userStreak.getNextStreakMilestone()} days
               Progress: ${(userStreak.getProgressToNextMilestone() * 100).toInt()}%
           """.trimIndent()

            // Display the detailed information in a long-duration toast
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } ?: run {
            // Handle case where no streak data is available yet
            Toast.makeText(this, "No streak data available yet. Start logging expenses!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows breakdown of points earned and badge progress when user taps total points.
     * Provides transparency about how points are calculated and motivates further engagement.
     */
    private fun showPointsBreakdown() {
        // Get current points and badge statistics from the adapter
        val earnedPoints = badgeAdapter.getEarnedPoints()
        val totalPossiblePoints = badgeAdapter.getTotalPossiblePoints()
        val earnedBadges = badgeAdapter.getTotalEarnedBadges()
        val totalBadges = viewModel.getAllBadges().size

        // Format comprehensive points breakdown message
        val message = """
           💎 POINTS BREAKDOWN
           
           Points Earned: $earnedPoints
           Total Possible: $totalPossiblePoints
           Completion: ${if (totalPossiblePoints > 0) (earnedPoints * 100 / totalPossiblePoints) else 0}%
           
           🏆 BADGES PROGRESS
           Badges Earned: $earnedBadges
           Total Badges: $totalBadges
           Badge Completion: ${if (totalBadges > 0) (earnedBadges * 100 / totalBadges) else 0}%
           
           Keep logging expenses to earn more points and badges!
       """.trimIndent()

        // Display the points breakdown information
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Called when activity becomes visible again. Forces refresh of all
     * gamification data to ensure UI reflects any changes made in other parts of the app.
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "=== ACTIVITY RESUMED ===")

        // **ENHANCED**: Comprehensive data refresh when returning to screen
        refreshAllData()
    }

    /**
     * Comprehensive refresh method that reloads all gamification data and updates UI.
     * This ensures the activity shows the most current information when user returns to it.
     */
    private fun refreshAllData() {
        try {
            Log.d(TAG, "Refreshing all gamification data")

            // Reinitialize user data to pick up any changes
            initializeUserData()

            // Check for any new badges that might have been earned
            viewModel.checkForImmediateBadges()

            // Force UI refresh after data loading with slight delay to ensure completion
            rvBadges.postDelayed({
                refreshUIComponents()
            }, 500)

            Log.d(TAG, "Data refresh completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing data: ${e.message}", e)
        }
    }

    /**
     * Called when activity is no longer visible. Logs state change for debugging purposes.
     */
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Activity paused")
    }

    /**
     * Called when activity is being destroyed. Logs state change for debugging and cleanup tracking.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity destroyed")
    }
}