package com.firstproject.prog7313_budgetbuddy.adapters

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.R
import com.firstproject.prog7313_budgetbuddy.data.models.Badge

class BadgeAdapter(
    private var badges: List<Badge>,
    private var earnedBadgeIds: List<String>
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBadgeIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)
        val tvBadgeName: TextView = itemView.findViewById(R.id.tvBadgeName)
        val tvBadgePoints: TextView = itemView.findViewById(R.id.tvBadgePoints)
        val viewBadgeOverlay: View = itemView.findViewById(R.id.viewBadgeOverlay)
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

        // Set badge icon based on badge ID
        val iconResId = when (badge.id) {
            "first_log" -> R.drawable.ic_badge_first
            "week_warrior" -> R.drawable.ic_badge_week
            "fortnight_champion" -> R.drawable.ic_badge_fortnight
            "monthly_master" -> R.drawable.ic_badge_month
            "streak_legend" -> R.drawable.ic_badge_legend
            else -> R.drawable.ic_badge_default
        }

        holder.ivBadgeIcon.setImageResource(iconResId)

        if (isEarned) {
            // Badge is earned - show in color
            holder.viewBadgeOverlay.visibility = View.GONE
            holder.ivBadgeIcon.colorFilter = null
            holder.tvBadgeName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.asparagus))
            holder.tvBadgePoints.visibility = View.VISIBLE
        } else {
            // Badge is locked - show in grayscale
            holder.viewBadgeOverlay.visibility = View.VISIBLE

            // Apply grayscale filter
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            holder.ivBadgeIcon.colorFilter = ColorMatrixColorFilter(colorMatrix)

            holder.tvBadgeName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
            holder.tvBadgePoints.visibility = View.GONE
        }

        // Add click listener to show badge details
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val message = if (isEarned) {
                "${badge.description}\nPoints earned: ${badge.points}"
            } else {
                "${badge.description}\nRequired: ${badge.requiredStreak} day streak"
            }

            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int = badges.size

    fun updateBadges(newBadges: List<Badge>, newEarnedBadgeIds: List<String>) {
        badges = newBadges
        earnedBadgeIds = newEarnedBadgeIds
        notifyDataSetChanged()
    }
}