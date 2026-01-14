package com.example.spendsense

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class TrackActivity : AppCompatActivity() {

    private lateinit var transactionManager: TransactionManager
    private lateinit var budgetManager: BudgetManager

    private lateinit var tabWeek: TextView
    private lateinit var tabMonth: TextView
    private lateinit var tabAll: TextView

    private var selectedPeriod = "week" // week, month, all

    // Category colors for the bars
    private val categoryColors = mapOf(
        "Food" to "#FF6B6B",
        "Transport" to "#4ECDC4",
        "Shopping" to "#45B7D1",
        "Entertainment" to "#96CEB4",
        "Bills" to "#FFEAA7",
        "Health" to "#DDA0DD",
        "Education" to "#98D8C8",
        "Other" to "#B8C5D6"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        transactionManager = TransactionManager(this)
        budgetManager = BudgetManager(this)

        setupViews()
        updateAnalytics()
    }

    private fun setupViews() {
        val backBtn: ImageView = findViewById(R.id.backBtn)
        tabWeek = findViewById(R.id.tabWeek)
        tabMonth = findViewById(R.id.tabMonth)
        tabAll = findViewById(R.id.tabAll)

        backBtn.setOnClickListener { finish() }

        tabWeek.setOnClickListener {
            selectedPeriod = "week"
            updateTabUI()
            updateAnalytics()
        }

        tabMonth.setOnClickListener {
            selectedPeriod = "month"
            updateTabUI()
            updateAnalytics()
        }

        tabAll.setOnClickListener {
            selectedPeriod = "all"
            updateTabUI()
            updateAnalytics()
        }
    }

    private fun updateTabUI() {
        // Reset all tabs
        tabWeek.setBackgroundResource(R.drawable.period_tab_unselected)
        tabMonth.setBackgroundResource(R.drawable.period_tab_unselected)
        tabAll.setBackgroundResource(R.drawable.period_tab_unselected)
        tabWeek.setTextColor(resources.getColor(R.color.text_secondary, null))
        tabMonth.setTextColor(resources.getColor(R.color.text_secondary, null))
        tabAll.setTextColor(resources.getColor(R.color.text_secondary, null))

        // Highlight selected
        when (selectedPeriod) {
            "week" -> {
                tabWeek.setBackgroundResource(R.drawable.period_tab_selected)
                tabWeek.setTextColor(Color.WHITE)
            }
            "month" -> {
                tabMonth.setBackgroundResource(R.drawable.period_tab_selected)
                tabMonth.setTextColor(Color.WHITE)
            }
            "all" -> {
                tabAll.setBackgroundResource(R.drawable.period_tab_selected)
                tabAll.setTextColor(Color.WHITE)
            }
        }
    }

    private fun updateAnalytics() {
        val transactions = getFilteredTransactions()
        val expenses = transactions.filter { it.type == "expense" }
        val income = transactions.filter { it.type == "income" }

        // Update summary values
        val totalSpentValue: TextView = findViewById(R.id.totalSpentValue)
        val totalIncomeValue: TextView = findViewById(R.id.totalIncomeValue)
        val expenseCount: TextView = findViewById(R.id.expenseCount)
        val incomeCount: TextView = findViewById(R.id.incomeCount)
        val totalCount: TextView = findViewById(R.id.totalCount)

        val totalSpent = expenses.sumOf { it.amount }
        val totalIncome = income.sumOf { it.amount }

        totalSpentValue.text = formatCurrency(totalSpent)
        totalIncomeValue.text = formatCurrency(totalIncome)
        expenseCount.text = expenses.size.toString()
        incomeCount.text = income.size.toString()
        totalCount.text = transactions.size.toString()

        // Update insight
        updateInsight(expenses)

        // Update budget breakdown
        updateBudgetBreakdown()

        // Update category breakdown
        updateCategoryBreakdown(expenses)
    }

    private fun getFilteredTransactions(): List<Transaction> {
        val allTransactions = transactionManager.getAllTransactions()
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (selectedPeriod) {
            "week" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis
                allTransactions.filter { it.timestamp >= weekAgo }
            }
            "month" -> {
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.timeInMillis
                allTransactions.filter { it.timestamp >= monthAgo }
            }
            else -> allTransactions
        }
    }

    private fun updateInsight(expenses: List<Transaction>) {
        val insightCard: LinearLayout = findViewById(R.id.insightCard)
        val insightText: TextView = findViewById(R.id.insightText)

        if (expenses.isEmpty()) {
            insightCard.visibility = View.GONE
            return
        }

        // Find top spending category
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        if (categoryTotals.isNotEmpty()) {
            val topCategory = categoryTotals.first()
            val periodText = when (selectedPeriod) {
                "week" -> "this week"
                "month" -> "this month"
                else -> "overall"
            }

            insightCard.visibility = View.VISIBLE
            insightText.text = "You spent most on ${topCategory.first} $periodText (${formatCurrency(topCategory.second)})"
        } else {
            insightCard.visibility = View.GONE
        }
    }

    private fun updateBudgetBreakdown() {
        val budgetBreakdownCard: LinearLayout = findViewById(R.id.budgetBreakdownCard)
        val needsAmount: TextView = findViewById(R.id.needsAmount)
        val savingsAmount: TextView = findViewById(R.id.savingsAmount)
        val wantsAmount: TextView = findViewById(R.id.wantsAmount)
        val needsProgress: View = findViewById(R.id.needsProgress)
        val savingsProgress: View = findViewById(R.id.savingsProgress)
        val wantsProgress: View = findViewById(R.id.wantsProgress)

        if (!budgetManager.hasBudget()) {
            budgetBreakdownCard.visibility = View.GONE
            return
        }

        budgetBreakdownCard.visibility = View.VISIBLE

        val needs = budgetManager.getNeeds()
        val savings = budgetManager.getSavings()
        val wants = budgetManager.getWants()
        val spent = budgetManager.getSpent()

        // For simplicity, distribute spent across categories proportionally
        // In a real app, you'd track spending per budget category
        val needsSpent = spent * 0.5
        val savingsSpent = spent * 0.2
        val wantsSpent = spent * 0.3

        needsAmount.text = "${formatCurrency(needsSpent)} / ${formatCurrency(needs)}"
        savingsAmount.text = "${formatCurrency(savingsSpent)} / ${formatCurrency(savings)}"
        wantsAmount.text = "${formatCurrency(wantsSpent)} / ${formatCurrency(wants)}"

        // Update progress bars
        updateProgressBar(needsProgress, needsSpent, needs)
        updateProgressBar(savingsProgress, savingsSpent, savings)
        updateProgressBar(wantsProgress, wantsSpent, wants)
    }

    private fun updateProgressBar(progressView: View, spent: Double, total: Double) {
        val percentage = if (total > 0) (spent / total).coerceIn(0.0, 1.0) else 0.0
        val params = progressView.layoutParams as LinearLayout.LayoutParams
        params.weight = percentage.toFloat()
        progressView.layoutParams = params
    }

    private fun updateCategoryBreakdown(expenses: List<Transaction>) {
        val container: LinearLayout = findViewById(R.id.categoryBreakdownContainer)
        val emptyState: LinearLayout = findViewById(R.id.emptyCategoryState)

        container.removeAllViews()

        if (expenses.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            return
        }

        emptyState.visibility = View.GONE

        // Group by category and calculate totals
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> 
                Pair(
                    entry.value.sumOf { it.amount },
                    entry.value.first().categoryIcon
                )
            }
            .toList()
            .sortedByDescending { it.second.first }

        val maxAmount = categoryTotals.maxOfOrNull { it.second.first } ?: 1.0
        val totalSpent = expenses.sumOf { it.amount }

        categoryTotals.forEach { (category, data) ->
            val amount = data.first
            val icon = data.second
            val percentage = if (totalSpent > 0) (amount / totalSpent * 100).toInt() else 0
            val barPercentage = amount / maxAmount

            val categoryView = createCategoryRow(icon, category, amount, percentage, barPercentage)
            container.addView(categoryView)
        }
    }

    private fun createCategoryRow(
        icon: String,
        category: String,
        amount: Double,
        percentage: Int,
        barPercentage: Double
    ): LinearLayout {
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            orientation = LinearLayout.VERTICAL
        }

        // Header row
        val headerRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val iconText = TextView(this).apply {
            text = icon
            textSize = 18f
            setPadding(0, 0, 12, 0)
        }
        headerRow.addView(iconText)

        val categoryText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = category
            textSize = 14f
            setTextColor(Color.WHITE)
        }
        headerRow.addView(categoryText)

        val percentText = TextView(this).apply {
            text = "$percentage%"
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            setPadding(0, 0, 12, 0)
        }
        headerRow.addView(percentText)

        val amountText = TextView(this).apply {
            text = formatCurrency(amount)
            textSize = 14f
            setTextColor(resources.getColor(R.color.expense_red, null))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        headerRow.addView(amountText)

        container.addView(headerRow)

        // Progress bar
        val progressContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8
            )
            setBackgroundResource(R.drawable.category_progress_background)
        }

        val progressBar = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                barPercentage.toFloat()
            )
            setBackgroundColor(Color.parseColor(categoryColors[category] ?: "#5B8DBE"))
        }
        progressContainer.addView(progressBar)

        // Add empty space to fill remaining
        val emptySpace = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 - barPercentage).toFloat()
            )
        }
        progressContainer.addView(emptySpace)

        container.addView(progressContainer)

        return container
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        return format.format(amount).replace("PHP", "₱")
    }
}
