package com.firstproject.prog7313_budgetbuddy.adapters

/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 2
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: Basic syntax | Kotlin Documentation
 Author: Kotlin
 Date Published: 06 November 2024
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://kotlinlang.org/docs/basic-syntax.html
  --------------------------------Code Attribution----------------------------------
*/

// Import necessary Android and Kotlin libraries
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
/*
 --------------------------------Code Attribution----------------------------------
 Title: Create dynamic lists with RecyclerView  |  Views  |  Android Developers
 Author: Android Developers
 Date Published: 2025
 Date Accessed: 18 April 2025
 Code Version: N/A
 Availability: https://developer.android.com/develop/ui/views/layout/recyclerview#next-steps
 --------------------------------Code Attribution----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: Adapter  |  API reference  |  Android Developers
 Author: Android Developer
 Date Published: 2019
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://developer.android.com/reference/android/widget/Adapter
  --------------------------------Code Attribution----------------------------------
*/
// Adapter for displaying category spending data in a RecyclerView
class CategorySpendingAdapter(
    // List of category spending data
    private var categories: List<CategoryWithAmount>
) : RecyclerView.Adapter<CategorySpendingAdapter.CategoryViewHolder>() {
    // Formatter to format amounts in South African Rand currency
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    // ViewHolder class to hold and manage views for each item in the RecyclerView
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryColorView: View = itemView.findViewById(R.id.vCategoryColor)
        val categoryNameText: TextView = itemView.findViewById(R.id.tvCategoryName)
        val amountText: TextView = itemView.findViewById(R.id.tvAmount)
        val percentageText: TextView = itemView.findViewById(R.id.tvPercentage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.pbCategory)
    }

    // Inflate the layout for each item and return a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_spending, parent, false)
        return CategoryViewHolder(view)
    }

    // Binds data to each item in the list
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // Set the background color of the category color view
        try {
            holder.categoryColorView.setBackgroundColor(Color.parseColor(category.colour))
            // Also set the progress bar's color programmatically
            holder.progressBar.progressDrawable.setColorFilter(
                Color.parseColor(category.colour),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } catch (e: IllegalArgumentException) {
            // Use a default color if the color string is invalid
            holder.categoryColorView.setBackgroundColor(Color.GRAY)
        }

        // Set the text views with category data
        holder.categoryNameText.text = category.categoryName
        holder.amountText.text = currencyFormat.format(category.amount)
        holder.percentageText.text = String.format("%.1f%%", category.percentage * 100)

        /// Set the progress bar to reflect the percentage (scaled to 0-100)
        holder.progressBar.progress = (category.percentage * 100).toInt()
    }

    // Return the number of categories
    override fun getItemCount(): Int = categories.size

    // Update the list of categories and refresh the RecyclerView
    fun updateCategories(newCategories: List<CategoryWithAmount>) {
        this.categories = newCategories
        notifyDataSetChanged()
    }
}