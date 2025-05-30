package com.firstproject.prog7313_budgetbuddy.adapters

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
    private var badges: List<Badge>,
    private var earnedBadgeIds: List<String>
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBadgeIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)
        val tvBadgeName: TextView = itemView.findViewById(R.id.tvBadgeName)
        val tvBadgePoints: TextView = itemView.findViewById(R.id.tvBadgePoints)
        val viewBadgeOverlay: View = itemView.findViewById(R.id.viewBadgeOverlay)
        val ivLockIcon: ImageView = itemView.findViewById(R.id.ivLockIcon)
        val tvBadgeType: TextView = itemView.findViewById(R.id.tvBadgeType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        val isEarned = earnedBadgeIds.contains(badge.id)

        holder.tvBadgeName.text = badge.name
        holder.tvBadgePoints.text = "+${badge.points} pts"

        // **FIXED**: Safer icon mapping with fallback
        val iconResId = getBadgeIconResource(badge.id)
        holder.ivBadgeIcon.setImageResource(iconResId)

        // **ENHANCED**: Set badge type indicator with better icons
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
            // Badge is earned - show in color
            holder.viewBadgeOverlay.visibility = View.GONE
            holder.ivLockIcon.visibility = View.GONE
            holder.ivBadgeIcon.colorFilter = null
            holder.tvBadgeName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.asparagus))
            holder.tvBadgePoints.visibility = View.VISIBLE
            holder.tvBadgeType.alpha = 1f

            // **ENHANCED**: Add golden glow for earned badges
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_gold))

            // **FIXED**: More subtle animation
            animateEarnedBadge(holder.ivBadgeIcon)
        } else {
            // Badge is locked - show in grayscale
            holder.viewBadgeOverlay.visibility = View.VISIBLE
            holder.ivLockIcon.visibility = View.VISIBLE

            // Apply grayscale filter
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            holder.ivBadgeIcon.colorFilter = ColorMatrixColorFilter(colorMatrix)

            holder.tvBadgeName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
            holder.tvBadgePoints.visibility = View.GONE
            holder.tvBadgeType.alpha = 0.5f
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
        }

        // **ENHANCED**: Comprehensive badge details
        holder.itemView.setOnClickListener {
            showBadgeDetails(holder.itemView.context, badge, isEarned)
        }

        // **NEW**: Long press for technical details
        holder.itemView.setOnLongClickListener {
            showTechnicalDetails(holder.itemView.context, badge, isEarned)
            true
        }
    }

    override fun getItemCount(): Int = badges.size

    fun updateBadges(newBadges: List<Badge>, newEarnedBadgeIds: List<String>) {
        badges = newBadges
        earnedBadgeIds = newEarnedBadgeIds
        notifyDataSetChanged()
    }

    // **FIXED**: Safe icon resource mapping with fallback
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

                // Default fallback
                else -> R.drawable.ic_star // Use star as default icon
            }
        } catch (e: Exception) {
            // If any drawable is missing, use a safe fallback
            R.drawable.ic_star
        }
    }

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

    private fun showBadgeDetails(context: android.content.Context, badge: Badge, isEarned: Boolean) {
        val typeDescription = when (badge.badgeType) {
            BadgeType.STREAK -> "Consecutive days logging expenses"
            BadgeType.EXPENSE_COUNT -> "Total expenses logged"
            BadgeType.BUDGET_KEEPER -> "Days staying under budget"
            BadgeType.CATEGORY_DIVERSITY -> "Different categories used"
            BadgeType.EARLY_BIRD -> "Expenses logged before 9 AM"
            BadgeType.WEEKEND_WARRIOR -> "Weekend days with expenses"
        }

        val requirement = when (badge.badgeType) {
            BadgeType.STREAK -> "${badge.requiredStreak} consecutive days"
            else -> "${badge.requiredValue} ${getRequirementUnit(badge.badgeType)}"
        }

        val statusIcon = if (isEarned) "🏆" else "🔒"
        val statusText = if (isEarned) "EARNED" else "LOCKED"

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

    // Public methods for analytics
    fun getBadgesByType(badgeType: BadgeType): List<Badge> {
        return badges.filter { it.badgeType == badgeType }
    }

    fun getEarnedBadgeCount(badgeType: BadgeType): Int {
        return badges.count { it.badgeType == badgeType && earnedBadgeIds.contains(it.id) }
    }

    fun getTotalEarnedBadges(): Int {
        return earnedBadgeIds.size
    }

    fun getTotalPossiblePoints(): Int {
        return badges.sumOf { it.points }
    }

    fun getEarnedPoints(): Int {
        return badges.filter { earnedBadgeIds.contains(it.id) }.sumOf { it.points }
    }
}