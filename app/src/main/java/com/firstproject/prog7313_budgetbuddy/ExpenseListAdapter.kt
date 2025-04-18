package com.firstproject.prog7313_budgetbuddy.adapters

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

class ExpenseListAdapter(
    private var expenses: List<Expense>,
    private val listener: ExpenseClickListener
) : RecyclerView.Adapter<ExpenseListAdapter.ExpenseViewHolder>() {

    private val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    interface ExpenseClickListener {
        fun onExpenseClicked(expense: Expense)
        fun onDownloadReceiptClicked(expense: Expense)
    }

    fun updateExpenses(newExpenses: List<Expense>) {
        this.expenses = newExpenses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayOfMonth: TextView = itemView.findViewById(R.id.tvDayOfMonth)
        private val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val btnDownload: ImageButton = itemView.findViewById(R.id.btnDownload)

        fun bind(expense: Expense) {
            // Set date
            tvDayOfMonth.text = dateFormat.format(expense.expenseDate)
            tvMonth.text = monthFormat.format(expense.expenseDate).uppercase()

            // Set category and description
            tvCategory.text = expense.category
            tvDescription.text = expense.description

            // Set amount in Rands
            tvAmount.text = currencyFormat.format(expense.totalAmount)

            // Manage download button visibility and click listener
            if (expense.photoId != null) {
                btnDownload.visibility = View.VISIBLE
                btnDownload.setOnClickListener {
                    listener.onDownloadReceiptClicked(expense)
                }
            } else {
                btnDownload.visibility = View.GONE
            }

            // Set click listener for entire item
            itemView.setOnClickListener {
                listener.onExpenseClicked(expense)
            }
        }
    }
}