package com.firstproject.prog7313_budgetbuddy

import android.os.Bundle
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
        loadStreakData()
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

        // Setup RecyclerView
        badgeAdapter = BadgeAdapter(emptyList(), emptyList())
        rvBadges.layoutManager = GridLayoutManager(this, 3)
        rvBadges.adapter = badgeAdapter

        // Setup theme toggle
        setupThemeToggle(btnThemeToggle)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadStreakData() {
        viewModel.getUserStreak().observe(this) { userStreak ->
            userStreak?.let {
                updateStreakDisplay(it)
                updateBadgeDisplay(it)
                checkTodayStatus(it)
            } ?: run {
                // Initialize streak if needed
                viewModel.initializeUserStreak()
            }
        }
    }

    private fun updateStreakDisplay(userStreak: UserStreak) {
        // Update streak numbers
        tvCurrentStreak.text = userStreak.currentStreak.toString()
        tvLongestStreak.text = userStreak.longestStreak.toString()
        tvTotalPoints.text = "${userStreak.points} pts"

        // Update streak message
        tvStreakMessage.text = when (userStreak.currentStreak) {
            0 -> "Start logging expenses to build your streak!"
            1 -> "Great start! Keep it going!"
            in 2..6 -> "Nice! ${7 - userStreak.currentStreak} more days to your first badge!"
            7 -> "Awesome! You've earned the Week Warrior badge!"
            in 8..13 -> "Keep going! ${14 - userStreak.currentStreak} more days to the next badge!"
            14 -> "Amazing! You've earned the Fortnight Champion badge!"
            in 15..29 -> "Incredible! ${30 - userStreak.currentStreak} more days to Monthly Master!"
            30 -> "Outstanding! You've earned the Monthly Master badge!"
            in 31..59 -> "Legendary! ${60 - userStreak.currentStreak} more days to Streak Legend!"
            else -> "You're a Streak Legend! Keep up the amazing work!"
        }

        // Animate streak number if it's high
        if (userStreak.currentStreak > 0) {
            tvCurrentStreak.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
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

    private fun updateBadgeDisplay(userStreak: UserStreak) {
        val allBadges = viewModel.getAllBadges()
        val earnedBadges = userStreak.badges

        // Update badge grid
        badgeAdapter.updateBadges(allBadges, earnedBadges)

        // Find next badge to earn
        val nextBadge = allBadges.firstOrNull { badge ->
            !earnedBadges.contains(badge.id) && badge.requiredStreak > userStreak.currentStreak
        }

        if (nextBadge != null) {
            tvNextBadgeName.text = "Next: ${nextBadge.name}"
            val progress = (userStreak.currentStreak.toFloat() / nextBadge.requiredStreak * 100).toInt()
            progressBarNextBadge.progress = progress
            tvNextBadgeProgress.text = "${userStreak.currentStreak}/${nextBadge.requiredStreak} days"
        } else {
            tvNextBadgeName.text = "All badges earned!"
            progressBarNextBadge.progress = 100
            tvNextBadgeProgress.text = "Congratulations!"
        }
    }

    private fun checkTodayStatus(userStreak: UserStreak) {
        // Check if user has logged today
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

        when {
            daysDifference == 0L -> {
                // Logged today
                tvLoggedToday.text = "✓ Expense logged today"
                tvLoggedToday.setTextColor(ContextCompat.getColor(this, R.color.olivine))
                viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.olivine))
            }
            daysDifference == 1L -> {
                // Haven't logged today but streak is safe
                tvLoggedToday.text = "⚠ Log an expense to continue streak!"
                tvLoggedToday.setTextColor(ContextCompat.getColor(this, R.color.gold))
                viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.gold))
            }
            else -> {
                // Streak will be broken
                tvLoggedToday.text = "✗ Streak broken - log expense to start new one"
                tvLoggedToday.setTextColor(ContextCompat.getColor(this, R.color.coral_pink))
                viewStreakIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral_pink))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this screen
        viewModel.getUserStreak().observe(this) { userStreak ->
            userStreak?.let {
                updateStreakDisplay(it)
                updateBadgeDisplay(it)
                checkTodayStatus(it)
            }
        }
    }
}