package com.firstproject.prog7313_budgetbuddy.adapters

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
 Title: RecyclerViewKotlin
 Author: Jeremy Walker
 Date Published: 30 July 2019
 Date Accessed: 30 May 2025
 Code Version: N/A
 Availability: https://github.com/android/views-widgets-samples/tree/main/RecyclerViewKotlin/

  --------------------------------Code Attribution----------------------------------
*/


import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.R
import com.firstproject.prog7313_budgetbuddy.data.models.Badge
import com.firstproject.prog7313_budgetbuddy.data.models.BadgeType

class BadgeAdapter(
    private var badges: List<Badge>,               // List of all badges to display
    private var earnedBadgeIds: List<String>       // List of badge IDs that the user has earned
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    // ViewHolder class holds references to each UI component of a badge item view
    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBadgeIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)       // Badge icon image
        val tvBadgeName: TextView = itemView.findViewById(R.id.tvBadgeName)        // Badge name text
        val tvBadgePoints: TextView = itemView.findViewById(R.id.tvBadgePoints)    // Points awarded by badge
        val viewBadgeOverlay: View = itemView.findViewById(R.id.viewBadgeOverlay)  // Overlay view for locked badges
        val ivLockIcon: ImageView = itemView.findViewById(R.id.ivLockIcon)         // Lock icon for locked badges
        val tvBadgeType: TextView = itemView.findViewById(R.id.tvBadgeType)        // Emoji indicator for badge type
    }

    // Inflate the badge item layout and create a ViewHolder for it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    // Bind badge data to the UI components of the ViewHolder at a given position
    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]                     // Get the badge at this position
        val isEarned = earnedBadgeIds.contains(badge.id) // Check if this badge is earned by the user

        // Set badge name and points text
        holder.tvBadgeName.text = badge.name
        holder.tvBadgePoints.text = "+${badge.points} pts"

        // Get the badge icon resource safely with fallback
        val iconResId = getBadgeIconResource(badge.id)
        holder.ivBadgeIcon.setImageResource(iconResId)

        // Set badge type emoji based on badge type enum
        val badgeTypeText = when (badge.badgeType) {
            BadgeType.STREAK -> "🔥"
            BadgeType.EXPENSE_COUNT -> "📝"
            BadgeType.BUDGET_KEEPER -> "💰"
            BadgeType.CATEGORY_DIVERSITY -> "🏷️"
            BadgeType.EARLY_BIRD -> "🌅"
            BadgeType.WEEKEND_WARRIOR -> "🎉"
        }
        holder.tvBadgeType.text = badgeTypeText

        if (isEarned) {
            // Badge earned: show full color, remove overlay and lock icon
            holder.viewBadgeOverlay.visibility = View.GONE
            holder.ivLockIcon.visibility = View.GONE
            holder.ivBadgeIcon.colorFilter = null // Remove grayscale filter if any
            holder.tvBadgeName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.asparagus))
            holder.tvBadgePoints.visibility = View.VISIBLE
            holder.tvBadgeType.alpha = 1f

            // Add a golden glow background to highlight earned badge
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_gold))

            // Animate the earned badge icon with a subtle scaling animation
            animateEarnedBadge(holder.ivBadgeIcon)
        } else {
            // Badge locked: apply grayscale overlay and show lock icon
            holder.viewBadgeOverlay.visibility = View.VISIBLE
            holder.ivLockIcon.visibility = View.VISIBLE

            // Apply grayscale color filter to icon
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            holder.ivBadgeIcon.colorFilter = ColorMatrixColorFilter(colorMatrix)

            // Set badge name text color to gray and hide points
            holder.tvBadgeName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
            holder.tvBadgePoints.visibility = View.GONE

            // Make badge type indicator semi-transparent
            holder.tvBadgeType.alpha = 0.5f

            // Remove any background highlight for locked badges
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
        }

        // Show detailed badge info on normal click
        holder.itemView.setOnClickListener {
            showBadgeDetails(holder.itemView.context, badge, isEarned)
        }

        // Show technical badge info on long press
        holder.itemView.setOnLongClickListener {
            showTechnicalDetails(holder.itemView.context, badge, isEarned)
            true
        }
    }

    // Return the total number of badges to be displayed
    override fun getItemCount(): Int = badges.size

    // Update the badge list and earned badge IDs, then refresh the RecyclerView
    fun updateBadges(newBadges: List<Badge>, newEarnedBadgeIds: List<String>) {
        badges = newBadges
        earnedBadgeIds = newEarnedBadgeIds
        notifyDataSetChanged()
    }

    // Map badge ID to drawable resource safely with a default fallback icon
    private fun getBadgeIconResource(badgeId: String): Int {
        return try {
            when (badgeId) {
                // Streak badges
                "first_log" -> R.drawable.ic_badge_first
                "week_warrior" -> R.drawable.ic_badge_week
                "fortnight_champion" -> R.drawable.ic_badge_fortnight
                "monthly_master" -> R.drawable.ic_badge_month
                "streak_legend" -> R.drawable.ic_badge_legend

                // Expense count badges
                "expense_rookie" -> R.drawable.ic_badge_expense_rookie
                "expense_veteran" -> R.drawable.ic_badge_expense_veteran
                "expense_master" -> R.drawable.ic_badge_expense_master

                // Budget badges
                "budget_keeper" -> R.drawable.ic_badge_budget_keeper
                "frugal_master" -> R.drawable.ic_badge_frugal_master

                // Category badges
                "category_explorer" -> R.drawable.ic_badge_category_explorer
                "category_master" -> R.drawable.ic_badge_category_master

                // Special behavior badges
                "early_bird" -> R.drawable.ic_badge_early_bird
                "weekend_warrior" -> R.drawable.ic_badge_weekend_warrior

                // Default fallback icon for unknown badge IDs
                else -> R.drawable.ic_star
            }
        } catch (e: Exception) {
            // Fallback icon in case resource loading fails
            R.drawable.ic_star
        }
    }

    // Animate the earned badge icon with a subtle scaling effect to highlight it
    private fun animateEarnedBadge(iconView: ImageView) {
        iconView.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(500)
            .withEndAction {
                iconView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .start()
            }
            .start()
    }

    // Show a Toast popup with detailed badge information when clicked
    private fun showBadgeDetails(context: android.content.Context, badge: Badge, isEarned: Boolean) {
        // Description of badge type
        val typeDescription = when (badge.badgeType) {
            BadgeType.STREAK -> "Consecutive days logging expenses"
            BadgeType.EXPENSE_COUNT -> "Total expenses logged"
            BadgeType.BUDGET_KEEPER -> "Days staying under budget"
            BadgeType.CATEGORY_DIVERSITY -> "Different categories used"
            BadgeType.EARLY_BIRD -> "Expenses logged before 9 AM"
            BadgeType.WEEKEND_WARRIOR -> "Weekend days with expenses"
        }

        // Display badge unlock requirement with proper units
        val requirement = when (badge.badgeType) {
            BadgeType.STREAK -> "${badge.requiredStreak} consecutive days"
            else -> "${badge.requiredValue} ${getRequirementUnit(badge.badgeType)}"
        }

        // Status icons and labels for earned vs locked badges
        val statusIcon = if (isEarned) "🏆" else "🔒"
        val statusText = if (isEarned) "EARNED" else "LOCKED"

        // Formatted detailed message to display
        val message = """
            $statusIcon ${badge.name} - $statusText
            
            ${badge.description}
            
            🎯 Requirement: $requirement
            💎 Points: ${badge.points}
            📋 Category: $typeDescription
            
            ${if (isEarned) "✅ Congratulations!" else "Keep going to unlock this badge!"}
        """.trimIndent()

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // Show technical badge details on long press for debug or info purposes
    private fun showTechnicalDetails(context: android.content.Context, badge: Badge, isEarned: Boolean) {
        val message = """
            🔧 Technical Details:
            
            Badge ID: ${badge.id}
            Type: ${badge.badgeType}
            Status: ${if (isEarned) "Earned ✅" else "Locked 🔒"}
            Points: ${badge.points}
            ${if (badge.badgeType == BadgeType.STREAK)
            "Required Streak: ${badge.requiredStreak}"
        else
            "Required Value: ${badge.requiredValue}"}
        """.trimIndent()

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Helper to get unit string for badge requirements based on badge type
    private fun getRequirementUnit(badgeType: BadgeType): String {
        return when (badgeType) {
            BadgeType.EXPENSE_COUNT -> "expenses"
            BadgeType.BUDGET_KEEPER -> "budget days"
            BadgeType.CATEGORY_DIVERSITY -> "categories"
            BadgeType.EARLY_BIRD -> "early logs"
            BadgeType.WEEKEND_WARRIOR -> "weekends"
            else -> "items"
        }
    }

    // Public method: Get all badges filtered by a specific badge type
    fun getBadgesByType(badgeType: BadgeType): List<Badge> {
        return badges.filter { it.badgeType == badgeType }
    }

    // Public method: Count how many badges of a given type have been earned
    fun getEarnedBadgeCount(badgeType: BadgeType): Int {
        return badges.count { it.badgeType == badgeType && earnedBadgeIds.contains(it.id) }
    }

    // Public method: Total number of badges earned by the user
    fun getTotalEarnedBadges(): Int {
        return earnedBadgeIds.size
    }

    // Public method: Sum of points for all possible badges
    fun getTotalPossiblePoints(): Int {
        return badges.sumOf { it.points }
    }

    // Public method: Sum of points from earned badges only
    fun getEarnedPoints(): Int {
        return badges.filter { earnedBadgeIds.contains(it.id) }.sumOf { it.points }
    }
}
