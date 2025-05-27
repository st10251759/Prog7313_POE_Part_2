package com.firstproject.prog7313_budgetbuddy.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.R
import com.firstproject.prog7313_budgetbuddy.data.models.CategoryWithAmount
import java.text.NumberFormat
import java.util.Locale

// Adapter for displaying spending data by category on the home screen
class HomeCategoryAdapter(
    private var categories: List<CategoryWithAmount> // List of category data with amount and percentage
) : RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder>() {

    // Formatter to display amounts in South African Rands
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    // ViewHolder class to hold references to views in each item
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryColorView: View = itemView.findViewById(R.id.vCategoryColor) // Colored indicator for category
        val categoryNameText: TextView = itemView.findViewById(R.id.tvCategoryName) // Category name text
        val amountText: TextView = itemView.findViewById(R.id.tvAmount) // Amount spent in this category
        val percentageText: TextView = itemView.findViewById(R.id.tvPercentage) // Percentage of total budget
        val progressBar: ProgressBar = itemView.findViewById(R.id.pbCategory) // Visual progress bar
    }

    // Creates and inflates the layout for each RecyclerView item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_spending, parent, false)
        return CategoryViewHolder(view)
    }

    // Binds data from a CategoryWithAmount object to the corresponding ViewHolder
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // Set the background color for the color indicator and progress bar
        try {
            val color = Color.parseColor(category.colour)
            holder.categoryColorView.setBackgroundColor(color)

            // Set the progress bar's color using modern approach
            holder.progressBar.progressTintList = ColorStateList.valueOf(color)
        } catch (e: IllegalArgumentException) {
            // Fallback to gray if an invalid color string is provided
            holder.categoryColorView.setBackgroundColor(Color.GRAY)
            holder.progressBar.progressTintList = ColorStateList.valueOf(Color.GRAY)
        }

        // Set category name, formatted amount, and percentage
        holder.categoryNameText.text = category.categoryName
        holder.amountText.text = formatCurrency(category.amount)
        holder.percentageText.text = String.format("%.1f%%", category.percentage * 100)

        // Set progress bar based on percentage (converted to int)
        holder.progressBar.progress = (category.percentage * 100).toInt()
    }

    // Helper function to format currency, replacing ZAR with 'R'
    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("ZAR", "R")
    }

    // Returns the number of items in the adapter
    override fun getItemCount(): Int = categories.size

    // Updates the list of categories and refreshes the RecyclerView
    fun updateCategories(newCategories: List<CategoryWithAmount>) {
        this.categories = newCategories
        notifyDataSetChanged() // Notifies the adapter that the data has changed
    }
}