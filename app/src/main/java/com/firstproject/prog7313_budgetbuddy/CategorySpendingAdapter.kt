package com.firstproject.prog7313_budgetbuddy.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.R
import com.firstproject.prog7313_budgetbuddy.data.entities.CategoryWithAmount
import java.text.NumberFormat
import java.util.Locale

class CategorySpendingAdapter(
    private var categories: List<CategoryWithAmount>
) : RecyclerView.Adapter<CategorySpendingAdapter.CategoryViewHolder>() {

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryColorView: View = itemView.findViewById(R.id.vCategoryColor)
        val categoryNameText: TextView = itemView.findViewById(R.id.tvCategoryName)
        val amountText: TextView = itemView.findViewById(R.id.tvAmount)
        val percentageText: TextView = itemView.findViewById(R.id.tvPercentage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.pbCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_spending, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // Set category color
        try {
            holder.categoryColorView.setBackgroundColor(Color.parseColor(category.colour))
            // Also set the progress bar's color programmatically
            holder.progressBar.progressDrawable.setColorFilter(
                Color.parseColor(category.colour),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } catch (e: IllegalArgumentException) {
            // Fallback color if invalid color string
            holder.categoryColorView.setBackgroundColor(Color.GRAY)
        }

        // Set text values
        holder.categoryNameText.text = category.categoryName
        holder.amountText.text = currencyFormat.format(category.amount)
        holder.percentageText.text = String.format("%.1f%%", category.percentage * 100)

        // Set progress bar value (percentage * 100 for proper display)
        holder.progressBar.progress = (category.percentage * 100).toInt()
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<CategoryWithAmount>) {
        this.categories = newCategories
        notifyDataSetChanged()
    }
}