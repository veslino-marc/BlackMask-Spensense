package com.example.spendsense

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RecordActivity : AppCompatActivity() {

    private lateinit var transactionManager: TransactionManager
    private lateinit var budgetManager: BudgetManager

    private lateinit var filterAll: TextView
    private lateinit var filterExpense: TextView
    private lateinit var filterIncome: TextView
    private lateinit var transactionsContainer: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var transactionCount: TextView
    private lateinit var totalAmount: TextView

    private var currentFilter = "all" // all, expense, income

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        transactionManager = TransactionManager(this)
        budgetManager = BudgetManager(this)

        setupViews()
        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun setupViews() {
        val backBtn: ImageView = findViewById(R.id.backBtn)
        filterAll = findViewById(R.id.filterAll)
        filterExpense = findViewById(R.id.filterExpense)
        filterIncome = findViewById(R.id.filterIncome)
        transactionsContainer = findViewById(R.id.transactionsContainer)
        emptyState = findViewById(R.id.emptyState)
        transactionCount = findViewById(R.id.transactionCount)
        totalAmount = findViewById(R.id.totalAmount)

        backBtn.setOnClickListener { finish() }

        filterAll.setOnClickListener {
            currentFilter = "all"
            updateFilterUI()
            loadTransactions()
        }

        filterExpense.setOnClickListener {
            currentFilter = "expense"
            updateFilterUI()
            loadTransactions()
        }

        filterIncome.setOnClickListener {
            currentFilter = "income"
            updateFilterUI()
            loadTransactions()
        }
    }

    private fun updateFilterUI() {
        // Reset all filters
        filterAll.setBackgroundResource(R.drawable.filter_chip_unselected)
        filterExpense.setBackgroundResource(R.drawable.filter_chip_unselected)
        filterIncome.setBackgroundResource(R.drawable.filter_chip_unselected)
        filterAll.setTextColor(resources.getColor(R.color.text_secondary, null))
        filterExpense.setTextColor(resources.getColor(R.color.text_secondary, null))
        filterIncome.setTextColor(resources.getColor(R.color.text_secondary, null))

        // Highlight selected
        when (currentFilter) {
            "all" -> {
                filterAll.setBackgroundResource(R.drawable.filter_chip_selected)
                filterAll.setTextColor(Color.WHITE)
            }
            "expense" -> {
                filterExpense.setBackgroundResource(R.drawable.filter_chip_selected)
                filterExpense.setTextColor(Color.WHITE)
            }
            "income" -> {
                filterIncome.setBackgroundResource(R.drawable.filter_chip_selected)
                filterIncome.setTextColor(Color.WHITE)
            }
        }
    }

    private fun loadTransactions() {
        val allTransactions = transactionManager.getAllTransactions()
        
        val filteredTransactions = when (currentFilter) {
            "expense" -> allTransactions.filter { it.type == "expense" }
            "income" -> allTransactions.filter { it.type == "income" }
            else -> allTransactions
        }

        transactionsContainer.removeAllViews()

        if (filteredTransactions.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            transactionCount.text = "0 transactions"
            totalAmount.text = "₱0.00"
            return
        }

        emptyState.visibility = View.GONE

        // Update summary
        val count = filteredTransactions.size
        transactionCount.text = "$count transaction${if (count != 1) "s" else ""}"

        val total = when (currentFilter) {
            "expense" -> -filteredTransactions.sumOf { it.amount }
            "income" -> filteredTransactions.sumOf { it.amount }
            else -> filteredTransactions.filter { it.type == "income" }.sumOf { it.amount } -
                    filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
        }

        val totalColor = when {
            currentFilter == "expense" -> R.color.expense_red
            currentFilter == "income" -> R.color.income_green
            total >= 0 -> R.color.income_green
            else -> R.color.expense_red
        }
        
        totalAmount.text = if (total >= 0) "+${formatCurrency(total)}" else formatCurrency(total)
        totalAmount.setTextColor(resources.getColor(totalColor, null))

        // Group transactions by date
        val groupedTransactions = filteredTransactions.groupBy { getDateGroup(it.timestamp) }

        groupedTransactions.forEach { (dateGroup, transactions) ->
            // Add date header
            val dateHeader = createDateHeader(dateGroup)
            transactionsContainer.addView(dateHeader)

            // Add transactions for this date
            transactions.forEach { transaction ->
                val transactionView = createTransactionItem(transaction)
                transactionsContainer.addView(transactionView)
            }
        }
    }

    private fun getDateGroup(timestamp: Long): String {
        val now = Calendar.getInstance()
        val transactionDate = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, transactionDate) -> "Today"
            isYesterday(now, transactionDate) -> "Yesterday"
            isSameWeek(now, transactionDate) -> "This Week"
            isSameMonth(now, transactionDate) -> "This Month"
            else -> {
                val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, date)
    }

    private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun createDateHeader(dateGroup: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
                bottomMargin = 12
            }
            text = dateGroup
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    private fun createTransactionItem(transaction: Transaction): LinearLayout {
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.transaction_item_background)
            setPadding(16, 16, 16, 16)
            isClickable = true
            isFocusable = true

            setOnLongClickListener {
                showDeleteDialog(transaction)
                true
            }
        }

        // Icon background
        val iconContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                marginEnd = 14
            }
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.progress_background)
        }

        val iconText = TextView(this).apply {
            text = transaction.categoryIcon
            textSize = 20f
            gravity = Gravity.CENTER
        }
        iconContainer.addView(iconText)
        container.addView(iconContainer)

        // Details
        val detailsContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }

        val categoryText = TextView(this).apply {
            text = transaction.category
            textSize = 15f
            setTextColor(Color.parseColor("#1E3A5F"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        detailsContainer.addView(categoryText)

        val timeText = TextView(this).apply {
            text = formatTime(transaction.timestamp)
            textSize = 12f
            setTextColor(Color.parseColor("#5A6978"))
        }
        detailsContainer.addView(timeText)

        if (transaction.note.isNotEmpty()) {
            val noteText = TextView(this).apply {
                text = transaction.note
                textSize = 12f
                setTextColor(Color.parseColor("#5A6978"))
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            detailsContainer.addView(noteText)
        }

        container.addView(detailsContainer)

        // Amount
        val amountContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }

        val amountText = TextView(this).apply {
            val prefix = if (transaction.type == "expense") "-" else "+"
            val color = if (transaction.type == "expense") "#EF5350" else "#66BB6A"
            text = "$prefix${formatCurrency(transaction.amount)}"
            textSize = 15f
            setTextColor(Color.parseColor(color))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        amountContainer.addView(amountText)

        val typeText = TextView(this).apply {
            text = if (transaction.type == "expense") "Expense" else "Income"
            textSize = 11f
            setTextColor(Color.parseColor("#8A9BAE"))
        }
        amountContainer.addView(typeText)

        container.addView(amountContainer)

        return container
    }

    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this ${transaction.category} transaction of ${formatCurrency(transaction.amount)}?")
            .setPositiveButton("Delete") { _, _ ->
                // If it was an expense, restore the budget
                if (transaction.type == "expense" && budgetManager.hasBudget()) {
                    val currentSpent = budgetManager.getSpent()
                    val newSpent = (currentSpent - transaction.amount).coerceAtLeast(0.0)
                    // We need to update the spent amount - let's add a method
                    budgetManager.setSpent(newSpent)
                }
                
                transactionManager.deleteTransaction(transaction.id)
                loadTransactions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        return format.format(kotlin.math.abs(amount)).replace("PHP", "₱")
    }
}
