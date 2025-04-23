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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.prog7313_budgetbuddy.R
import com.firstproject.prog7313_budgetbuddy.data.entities.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
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
// Adapter class for displaying a list of expenses in a RecyclerView
class ExpenseListAdapter(
    private var expenses: List<Expense>, // List of Expense items to display
    private val listener: ExpenseClickListener // Listener interface for handling item interactions
) : RecyclerView.Adapter<ExpenseListAdapter.ExpenseViewHolder>() {

    // Formatters for date and currency
    private val dateFormat = SimpleDateFormat("d", Locale.getDefault()) // Day of the month
    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault()) // Abbreviated month name
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")) // Format in South African Rands

    // Interface for handling click events on expenses
    interface ExpenseClickListener {
        fun onExpenseClicked(expense: Expense) // When an expense item is clicked
        fun onDownloadReceiptClicked(expense: Expense) // When the download button is clicked
    }

    // Method to update the list of expenses and refresh the RecyclerView
    fun updateExpenses(newExpenses: List<Expense>) {
        this.expenses = newExpenses
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense) // Bind the expense to the ViewHolder
    }

    // Returns the total number of items
    override fun getItemCount(): Int = expenses.size

    // Inner ViewHolder class that manages individual item views
    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // View references from the layout
        private val tvDayOfMonth: TextView = itemView.findViewById(R.id.tvDayOfMonth)
        private val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val btnDownload: ImageButton = itemView.findViewById(R.id.btnDownload)

        // Binds the data from an Expense object to the item views
        fun bind(expense: Expense) {
            // Format and display the day and month of the expense date
            tvDayOfMonth.text = dateFormat.format(expense.expenseDate)
            tvMonth.text = monthFormat.format(expense.expenseDate).uppercase()

            // Set category and description texts
            tvCategory.text = expense.category
            tvDescription.text = expense.description

            // Format and display the total amount in currency format
            tvAmount.text = currencyFormat.format(expense.totalAmount)

            // Show the download button only if a photoId is available
            if (expense.photoId != null) {
                btnDownload.visibility = View.VISIBLE
                btnDownload.setOnClickListener {
                    listener.onDownloadReceiptClicked(expense) // Handle download click
                }
            } else {
                btnDownload.visibility = View.GONE // Hide download button if no receipt
            }

            // Handle click on the entire expense item
            itemView.setOnClickListener {
                listener.onExpenseClicked(expense) // Handle item click
            }
        }
    }
}