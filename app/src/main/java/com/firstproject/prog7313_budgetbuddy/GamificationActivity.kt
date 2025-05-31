package com.firstproject.prog7313_budgetbuddy

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

class GamificationActivity : BaseActivity() {

    private lateinit var viewModel: ViewModels
    private lateinit var auth: FirebaseAuth
    private lateinit var badgeAdapter: BadgeAdapter

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var btnThemeToggle: ImageButton
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvLongestStreak: TextView
    private lateinit var tvTotalPoints: TextView
    private lateinit var tvStreakMessage: TextView
    private lateinit var progressBarNextBadge: ProgressBar
    private lateinit var tvNextBadgeName: TextView
    private lateinit var tvNextBadgeProgress: TextView
    private lateinit var rvBadges: RecyclerView
    private lateinit var tvLoggedToday: TextView
    private lateinit var viewStreakIndicator: View

    // **ENHANCED**: Additional UI components
    private lateinit var tvStreakLevel: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvCategoriesUsed: TextView
    private lateinit var tvEarlyBirdCount: TextView
    private lateinit var tvWeekendCount: TextView
    private lateinit var progressBarOverall: ProgressBar

    companion object {
        private const val TAG = "GamificationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gamification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize UI
        initializeUI()
        setupListeners()

        // **FIXED**: Ensure user streak is initialized before loading
        initializeUserData()
    }

    private fun initializeUI() {
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

        // **ENHANCED**: Initialize additional UI components
        tvStreakLevel = findViewById(R.id.tvStreakLevel)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvCategoriesUsed = findViewById(R.id.tvCategoriesUsed)
        tvEarlyBirdCount = findViewById(R.id.tvEarlyBirdCount)
        tvWeekendCount = findViewById(R.id.tvWeekendCount)
        progressBarOverall = findViewById(R.id.progressBarOverall)

        // Setup RecyclerView
        badgeAdapter = BadgeAdapter(emptyList(), emptyList())
        rvBadges.layoutManager = GridLayoutManager(this, 3)
        rvBadges.adapter = badgeAdapter

        // Setup theme toggle if BaseActivity provides it
        try {
            setupThemeToggle(btnThemeToggle)
        } catch (e: Exception) {
            Log.w(TAG, "Theme toggle not available: ${e.message}")
            btnThemeToggle.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        // **ENHANCED**: Detailed streak info on long press
        tvCurrentStreak.setOnLongClickListener {
            showDetailedStreakInfo()
            true
        }

        // **NEW**: Points breakdown on click
        tvTotalPoints.setOnClickListener {
            showPointsBreakdown()
        }
    }

    // **FIXED**: Proper initialization sequence
    private fun initializeUserData() {
        Log.d(TAG, "=== INITIALIZING USER DATA ===")

        // **STEP 1**: Ensure user streak is initialized
        viewModel.initializeUserStreak()

        // **STEP 2**: Set up observers with proper error handling
        setupStreakObserver()

        // **STEP 3**: Load additional data
        loadDetailedStats()
    }

    private fun setupStreakObserver() {
        viewModel.getUserStreak().observe(this) { userStreak ->
            try {
                if (userStreak != null) {
                    Log.d(TAG, "✅ User streak loaded: ${userStreak.currentStreak} days, ${userStreak.totalExpensesLogged} expenses")
                    updateStreakDisplay(userStreak)
                    updateBadgeDisplay(userStreak)
                    checkTodayStatus(userStreak)

                    // **NEW**: Force UI refresh after data update
                    refreshUIComponents()
                } else {
                    Log.d(TAG, "⚠️ User streak is null, showing defaults")
                    showDefaultState()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in streak observer: ${e.message}", e)
                showDefaultState()
            }
        }
    }

    private fun refreshUIComponents() {
        try {
            // Force RecyclerView to refresh
            rvBadges.adapter?.notifyDataSetChanged()

            // Trigger layout refresh
            rvBadges.invalidate()

            Log.d(TAG, "UI components refreshed")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing UI: ${e.message}", e)
        }
    }

    // **NEW**: Show default state when no streak data
    // **FIXED**: Show proper default state for new users
    private fun showDefaultState() {
        // Basic stats
        tvCurrentStreak.text = "0"
        tvLongestStreak.text = "0"
        tvTotalPoints.text = "0 pts"
        tvStreakLevel.text = "Newcomer"
        tvStreakMessage.text = "Start logging expenses to build your streak! 🚀"
        tvLoggedToday.text = "No expenses logged yet"

        // **FIXED**: Detailed stats for new users
        tvTotalExpenses.text = "📝 0 expenses logged"
        tvCategoriesUsed.text = "🏷️ 0 categories explored"
        tvEarlyBirdCount.text = "🌅 0 early bird logs"
        tvWeekendCount.text = "🎉 0 weekend entries"

        // **FIX**: Set progress bar to 0 for new users
        progressBarOverall.progress = 0

        // **FIXED**: Show first badge as next target with 0 progress
        val allBadges = viewModel.getAllBadges()
        val firstBadge = allBadges.find { it.id == "first_log" } ?: allBadges.firstOrNull()

        if (firstBadge != null) {
            tvNextBadgeName.text = "Next: ${firstBadge.name}"
            progressBarNextBadge.progress = 0  // **CRITICAL**: Show 0 progress

            val requirement = if (firstBadge.badgeType == BadgeType.STREAK) {
                firstBadge.requiredStreak
            } else {
                firstBadge.requiredValue
            }

            val unit = getBadgeTypeUnit(firstBadge.badgeType)
            tvNextBadgeProgress.text = "0/$requirement $unit"
        } else {
            // Fallback if no badges found
            tvNextBadgeName.text = "Start your journey!"
            progressBarNextBadge.progress = 0
            tvNextBadgeProgress.text = "Log your first expense"
        }

        // Load all badges as unearned
        badgeAdapter.updateBadges(allBadges, emptyList())

        // **FIX**: Set proper today status for new users
        tvLoggedToday.text = "Ready to log your first expense?"
        tvLoggedToday.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.asparagus))

        Log.d(TAG, "Showing default state for new user")
    }

    private fun loadDetailedStats() {
        viewModel.getDetailedStats().observe(this) { stats ->
            updateDetailedStatsDisplay(stats)
        }

        viewModel.getNextBadges().observe(this) { nextBadges ->
            updateNextBadgeInfo(nextBadges)
        }
    }

    private fun updateStreakDisplay(userStreak: UserStreak) {
        try {
            Log.d(TAG, "Updating streak display with: streak=${userStreak.currentStreak}, points=${userStreak.points}")

            // **FIXED**: Safe string conversion and validation
            tvCurrentStreak.text = userStreak.currentStreak.toString()
            tvLongestStreak.text = userStreak.longestStreak.toString()
            tvTotalPoints.text = "${userStreak.points} pts"

            // **ENHANCED**: Dynamic streak level display
            val streakLevel = userStreak.getStreakLevel()
            tvStreakLevel.text = streakLevel

            // **IMPROVED**: More encouraging messages
            tvStreakMessage.text = getStreakMessage(userStreak.currentStreak)

            // **ENHANCED**: Animated streak number
            animateStreakNumber(userStreak.currentStreak)

            Log.d(TAG, "Streak display updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating streak display: ${e.message}", e)
        }
    }


    // **FIXED**: Accurate streak messages
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

    // **NEW**: Animate streak number based on achievement level
    private fun animateStreakNumber(streak: Int) {
        val scale = when {
            streak >= 60 -> 1.3f
            streak >= 30 -> 1.25f
            streak >= 14 -> 1.2f
            streak >= 7 -> 1.15f
            else -> 1.1f
        }

        if (streak > 0) {
            tvCurrentStreak.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(300)
                .withEndAction {
                    tvCurrentStreak.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start()
                }
                .start()
        }
    }

    // **ENHANCED**: Comprehensive detailed stats display
    private fun updateDetailedStatsDisplay(stats: GamificationStats) {
        tvTotalExpenses.text = "📝 ${stats.totalExpenses} expenses logged"
        tvCategoriesUsed.text = "🏷️ ${stats.categoriesUsed} categories explored"
        tvEarlyBirdCount.text = "🌅 ${stats.earlyBirdCount} early bird logs"
        tvWeekendCount.text = "🎉 ${stats.weekendCount} weekend entries"

        // **FIXED**: More accurate progress calculation
        val progress = (stats.progressToNext * 100).toInt().coerceIn(0, 100)
        progressBarOverall.progress = progress

        Log.d(TAG, "Updated stats: expenses=${stats.totalExpenses}, categories=${stats.categoriesUsed}, progress=$progress%")
    }

    private fun updateBadgeDisplay(userStreak: UserStreak) {
        try {
            val allBadges = viewModel.getAllBadges()
            val earnedBadges = userStreak.badges

            Log.d(TAG, "Updating badge display: ${earnedBadges.size}/${allBadges.size} badges earned")
            Log.d(TAG, "Earned badges: $earnedBadges")

            // Update badge grid
            badgeAdapter.updateBadges(allBadges, earnedBadges)

            // **ENHANCED**: Show progress for the closest badge
            updateNextBadgeProgress(userStreak, allBadges, earnedBadges)

            Log.d(TAG, "Badge display updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating badge display: ${e.message}", e)
        }
    }


    // **FIXED**: More accurate next badge progress calculation
    private fun updateNextBadgeProgress(userStreak: UserStreak, allBadges: List<Badge>, earnedBadges: List<String>) {
        // **FIX**: Find the NEXT badge to work towards, not the highest progress
        var nextBadge: Badge? = null
        var lowestRequirement = Int.MAX_VALUE

        // **CORRECTED**: Look for the badge with lowest requirement that's not yet earned
        for (badge in allBadges) {
            if (!earnedBadges.contains(badge.id)) {
                val requirement = if (badge.badgeType == BadgeType.STREAK) {
                    badge.requiredStreak
                } else {
                    badge.requiredValue
                }

                // **FIX**: Find the badge with the lowest requirement (closest to achieve)
                if (requirement < lowestRequirement) {
                    lowestRequirement = requirement
                    nextBadge = badge
                }
            }
        }

        if (nextBadge != null) {
            // **FIXED**: Calculate actual progress towards the next badge
            val progress = calculateBadgeProgress(userStreak, nextBadge)
            displayNextBadgeInfo(nextBadge, userStreak, progress)

            Log.d(TAG, "Next badge: ${nextBadge.name}, actual progress: ${(progress * 100).toInt()}%")
        } else {
            // Only show "all badges earned" if user actually has badges
            if (earnedBadges.isNotEmpty()) {
                displayAllBadgesEarned()
            } else {
                // **NEW**: Show proper state for new users
                displayFirstBadgePrompt()
            }
        }
    }

    // **NEW**: Show appropriate message for new users with no progress
    private fun displayFirstBadgePrompt() {
        val firstBadge = viewModel.getAllBadges().find { it.id == "first_log" }

        if (firstBadge != null) {
            tvNextBadgeName.text = "Next: ${firstBadge.name}"
            progressBarNextBadge.progress = 0  // **FIX**: Show 0 progress for new users
            tvNextBadgeProgress.text = "0/${firstBadge.requiredStreak} ${getBadgeTypeUnit(firstBadge.badgeType)}"

            Log.d(TAG, "Showing first badge prompt for new user")
        } else {
            // Fallback if first badge not found
            tvNextBadgeName.text = "Start logging expenses!"
            progressBarNextBadge.progress = 0
            tvNextBadgeProgress.text = "0/1 expenses"
        }
    }


    // **NEW**: Calculate accurate progress for any badge type
    private fun calculateBadgeProgress(userStreak: UserStreak, badge: Badge): Float {
        val currentValue = when (badge.badgeType) {
            BadgeType.STREAK -> userStreak.currentStreak
            BadgeType.EXPENSE_COUNT -> userStreak.totalExpensesLogged
            BadgeType.CATEGORY_DIVERSITY -> userStreak.categoriesUsed.size
            BadgeType.EARLY_BIRD -> userStreak.earlyBirdCount
            BadgeType.WEEKEND_WARRIOR -> userStreak.weekendLogCount
            BadgeType.BUDGET_KEEPER -> userStreak.budgetKeeperDays
        }

        val requiredValue = if (badge.badgeType == BadgeType.STREAK) {
            badge.requiredStreak
        } else {
            badge.requiredValue
        }

        // **FIX**: Ensure we don't divide by zero and handle edge cases
        return if (requiredValue > 0) {
            (currentValue.toFloat() / requiredValue).coerceIn(0f, 1f)
        } else {
            0f
        }
    }


    private fun displayNextBadgeInfo(badge: Badge, userStreak: UserStreak, progress: Float) {
        tvNextBadgeName.text = "Next: ${badge.name}"

        // **FIX**: Ensure progress is never negative and shows 0 for new users
        val progressPercentage = (progress * 100).toInt().coerceIn(0, 100)
        progressBarNextBadge.progress = progressPercentage

        val currentValue = when (badge.badgeType) {
            BadgeType.STREAK -> userStreak.currentStreak
            BadgeType.EXPENSE_COUNT -> userStreak.totalExpensesLogged
            BadgeType.CATEGORY_DIVERSITY -> userStreak.categoriesUsed.size
            BadgeType.EARLY_BIRD -> userStreak.earlyBirdCount
            BadgeType.WEEKEND_WARRIOR -> userStreak.weekendLogCount
            BadgeType.BUDGET_KEEPER -> userStreak.budgetKeeperDays
        }

        val requiredValue = if (badge.badgeType == BadgeType.STREAK) {
            badge.requiredStreak
        } else {
            badge.requiredValue
        }

        val unit = getBadgeTypeUnit(badge.badgeType)
        tvNextBadgeProgress.text = "$currentValue/$requiredValue $unit"

        Log.d(TAG, "Displaying badge: ${badge.name}, current: $currentValue, required: $requiredValue, progress: $progressPercentage%")
    }

    private fun displayAllBadgesEarned() {
        tvNextBadgeName.text = "All badges earned! 🎉"
        progressBarNextBadge.progress = 100
        tvNextBadgeProgress.text = "Congratulations!"

        Log.d(TAG, "User has earned all available badges!")
    }

    // **IMPROVED**: More descriptive unit names
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

    private fun updateNextBadgeInfo(nextBadges: List<Badge>) {
        // **NEW**: Could show multiple upcoming badges or cycle through them
        if (nextBadges.isNotEmpty()) {
            Log.d(TAG, "Next badges available: ${nextBadges.size}")
            // Future enhancement: show multiple next badges in a carousel
        }
    }

    // **FIXED**: More accurate today status using calendar days
    private fun checkTodayStatus(userStreak: UserStreak) {
        try {
            val lastLogCalendar = Calendar.getInstance().apply {
                time = userStreak.getLastLogDateAsDate()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

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

    private fun updateTodayStatusDisplay(daysDifference: Long) {
        val (message, colorRes, indicatorColorRes) = when {
            daysDifference == 0L -> {
                Triple(
                    "✅ Expense logged today - streak safe!",
                    R.color.olivine,
                    R.color.olivine
                )
            }
            daysDifference == 1L -> {
                Triple(
                    "⚠️ Log an expense today to continue your streak!",
                    R.color.gold,
                    R.color.gold
                )
            }
            else -> {
                Triple(
                    "❌ Streak broken - log an expense to start fresh!",
                    R.color.coral_pink,
                    R.color.coral_pink
                )
            }
        }

        tvLoggedToday.text = message
        tvLoggedToday.setTextColor(ContextCompat.getColor(this, colorRes))
        viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, indicatorColorRes))
    }

    private fun showDefaultTodayStatus() {
        tvLoggedToday.text = "Ready to log your first expense?"
        tvLoggedToday.setTextColor(ContextCompat.getColor(this, R.color.asparagus))
        viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.asparagus))
    }

    // **NEW**: Show detailed streak information
    private fun showDetailedStreakInfo() {
        viewModel.getUserStreak().value?.let { userStreak ->
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

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(this, "No streak data available yet. Start logging expenses!", Toast.LENGTH_SHORT).show()
        }
    }

    // **NEW**: Show points breakdown
    private fun showPointsBreakdown() {
        val earnedPoints = badgeAdapter.getEarnedPoints()
        val totalPossiblePoints = badgeAdapter.getTotalPossiblePoints()
        val earnedBadges = badgeAdapter.getTotalEarnedBadges()
        val totalBadges = viewModel.getAllBadges().size

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

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // **ENHANCED**: Better onResume with data refresh
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "=== ACTIVITY RESUMED ===")

        // **ENHANCED**: Force refresh all data when returning to screen
        refreshAllData()
    }

    private fun refreshAllData() {
        try {
            Log.d(TAG, "Refreshing all gamification data")

            // Re-initialize user data
            initializeUserData()

            // Check for immediate badge opportunities
            viewModel.checkForImmediateBadges()

            // Force UI refresh after a short delay to ensure data is loaded
            rvBadges.postDelayed({
                refreshUIComponents()
            }, 500)

            Log.d(TAG, "Data refresh completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing data: ${e.message}", e)
        }
    }


    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Activity paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity destroyed")
    }
}