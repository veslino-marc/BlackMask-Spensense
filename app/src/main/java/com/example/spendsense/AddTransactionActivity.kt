package com.example.spendsense

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var transactionManager: TransactionManager
    private lateinit var budgetManager: BudgetManager
    
    private var isExpense = true
    private var selectedCategory = ""
    private var selectedCategoryIcon = ""
    
    // Category views
    private lateinit var categoryViews: List<LinearLayout>
    
    // Expense categories
    private val expenseCategories = listOf(
        Pair("🍔", "Food"),
        Pair("🚗", "Transport"),
        Pair("🛒", "Shopping"),
        Pair("🎮", "Entertainment"),
        Pair("💡", "Bills"),
        Pair("💊", "Health"),
        Pair("📚", "Education"),
        Pair("📦", "Other")
    )
    
    // Income categories
    private val incomeCategories = listOf(
        Pair("💵", "Salary"),
        Pair("🎁", "Allowance"),
        Pair("💼", "Freelance"),
        Pair("🎀", "Gift"),
        Pair("💳", "Refund"),
        Pair("📈", "Investment"),
        Pair("🏦", "Savings"),
        Pair("📦", "Other")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        transactionManager = TransactionManager(this)
        budgetManager = BudgetManager(this)

        // Check if opened with specific type
        val type = intent.getStringExtra("type") ?: "expense"
        isExpense = type == "expense"

        setupViews()
        updateTabUI()
        updateCategories()
    }

    private fun setupViews() {
        val backBtn: ImageView = findViewById(R.id.backBtn)
        val tabExpense: LinearLayout = findViewById(R.id.tabExpense)
        val tabIncome: LinearLayout = findViewById(R.id.tabIncome)
        val saveBtn: Button = findViewById(R.id.saveBtn)

        // Initialize category views
        categoryViews = listOf(
            findViewById(R.id.cat1),
            findViewById(R.id.cat2),
            findViewById(R.id.cat3),
            findViewById(R.id.cat4),
            findViewById(R.id.cat5),
            findViewById(R.id.cat6),
            findViewById(R.id.cat7),
            findViewById(R.id.cat8)
        )

        backBtn.setOnClickListener { finish() }

        tabExpense.setOnClickListener {
            if (!isExpense) {
                isExpense = true
                updateTabUI()
                updateCategories()
                clearCategorySelection()
            }
        }

        tabIncome.setOnClickListener {
            if (isExpense) {
                isExpense = false
                updateTabUI()
                updateCategories()
                clearCategorySelection()
            }
        }

        // Setup category click listeners
        categoryViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                selectCategory(index)
            }
        }

        saveBtn.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateTabUI() {
        val tabExpense: LinearLayout = findViewById(R.id.tabExpense)
        val tabIncome: LinearLayout = findViewById(R.id.tabIncome)
        val tabExpenseText: TextView = findViewById(R.id.tabExpenseText)
        val tabIncomeText: TextView = findViewById(R.id.tabIncomeText)
        val amountLabel: TextView = findViewById(R.id.amountLabel)

        if (isExpense) {
            tabExpense.setBackgroundResource(R.drawable.tab_expense_selected)
            tabIncome.setBackgroundResource(R.drawable.tab_unselected)
            tabExpenseText.setTextColor(resources.getColor(R.color.white, null))
            tabIncomeText.setTextColor(resources.getColor(R.color.text_secondary, null))
            amountLabel.text = "How much did you spend?"
        } else {
            tabExpense.setBackgroundResource(R.drawable.tab_unselected)
            tabIncome.setBackgroundResource(R.drawable.tab_income_selected)
            tabExpenseText.setTextColor(resources.getColor(R.color.text_secondary, null))
            tabIncomeText.setTextColor(resources.getColor(R.color.white, null))
            amountLabel.text = "How much did you receive?"
        }
    }

    private fun updateCategories() {
        val categories = if (isExpense) expenseCategories else incomeCategories
        
        categoryViews.forEachIndexed { index, view ->
            val iconView = view.findViewById<TextView>(
                resources.getIdentifier("cat${index + 1}Icon", "id", packageName)
            )
            val textView = view.findViewById<TextView>(
                resources.getIdentifier("cat${index + 1}Text", "id", packageName)
            )
            
            iconView.text = categories[index].first
            textView.text = categories[index].second
        }
    }

    private fun selectCategory(index: Int) {
        val categories = if (isExpense) expenseCategories else incomeCategories
        
        // Clear all selections
        categoryViews.forEach { view ->
            view.isSelected = false
            view.setBackgroundResource(R.drawable.category_item_background)
        }
        
        // Select the clicked one
        categoryViews[index].isSelected = true
        categoryViews[index].setBackgroundResource(R.drawable.schedule_option_selected)
        
        selectedCategoryIcon = categories[index].first
        selectedCategory = categories[index].second
    }

    private fun clearCategorySelection() {
        categoryViews.forEach { view ->
            view.isSelected = false
            view.setBackgroundResource(R.drawable.category_item_background)
        }
        selectedCategory = ""
        selectedCategoryIcon = ""
    }

    private fun saveTransaction() {
        val amountInput: EditText = findViewById(R.id.amountInput)
        val noteInput: EditText = findViewById(R.id.noteInput)

        val amountText = amountInput.text.toString()
        
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = transactionManager.generateId(),
            type = if (isExpense) "expense" else "income",
            amount = amount,
            category = selectedCategory,
            categoryIcon = selectedCategoryIcon,
            note = noteInput.text.toString(),
            timestamp = System.currentTimeMillis()
        )

        transactionManager.saveTransaction(transaction)

        // Update budget spent if it's an expense
        if (isExpense && budgetManager.hasBudget()) {
            budgetManager.addExpense(amount)
        }

        val message = if (isExpense) "Expense added!" else "Income added!"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        
        finish()
    }
}
