package com.example.spendsense

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var budgetManager: BudgetManager
    private lateinit var transactionManager: TransactionManager
    private lateinit var navHome: ImageView
    private lateinit var navTrack: ImageView
    private lateinit var navAdd: ImageView
    private lateinit var navRecord: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userManager = UserManager(this)
        budgetManager = BudgetManager(this)
        transactionManager = TransactionManager(this)

        // Set username
        val greeting: TextView = findViewById(R.id.greeting)
        val username: TextView = findViewById(R.id.username)

        greeting.text = getGreeting()
        val displayName = userManager.getUsername()
        username.text = displayName

        // Initialize navigation icons
        navHome = findViewById(R.id.navHome)
        navTrack = findViewById(R.id.navTrack)
        navAdd = findViewById(R.id.navAdd)
        navRecord = findViewById(R.id.navRecord)

        // Set home as active
        setHomeActive()

        // Setup navigation
        setupNavigation()

        // Create Budget button
        val createBudgetBtn: Button = findViewById(R.id.createBudgetBtn)
        createBudgetBtn.setOnClickListener {
            startActivity(Intent(this, SetupPlan1Activity::class.java))
        }

        // Action buttons
        val addExpenseBtn: LinearLayout = findViewById(R.id.addExpenseBtn)
        val addCashBtn: LinearLayout = findViewById(R.id.addCashBtn)
        val profileIcon: ImageView = findViewById(R.id.profileIcon)

        addExpenseBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", "expense")
            startActivity(intent)
        }

        addCashBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", "income")
            startActivity(intent)
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Update displays
        updateBudgetDisplay()
        updateRecentTransactions()
    }

    override fun onResume() {
        super.onResume()
        updateBudgetDisplay()
        updateRecentTransactions()
    }

    private fun updateBudgetDisplay() {
        val budgetStatusCard = findViewById<LinearLayout>(R.id.budgetStatusCard)
        val budgetAmountText = findViewById<TextView>(R.id.budgetAmountText)
        val budgetPercentText = findViewById<TextView>(R.id.budgetPercentText)
        val budgetProgressBar = findViewById<View>(R.id.budgetProgressBar)
        val alertCard = findViewById<LinearLayout>(R.id.alertCard)
        val alertText = findViewById<TextView>(R.id.alertText)
        val createBudgetBtn: Button = findViewById(R.id.createBudgetBtn)

        if (budgetManager.hasBudget()) {
            // Show budget status
            budgetStatusCard?.visibility = View.VISIBLE
            
            val total = budgetManager.getTotalBudget()
            val spent = budgetManager.getSpent()
            val percentage = budgetManager.getUsagePercentage()

            budgetAmountText?.text = "${formatCurrency(spent)} / ${formatCurrency(total)}"
            budgetPercentText?.text = "$percentage%"

            // Update progress bar width
            budgetProgressBar?.let { bar ->
                val params = bar.layoutParams as? LinearLayout.LayoutParams
                params?.weight = percentage / 100f
                bar.layoutParams = params
            }

            // Update create budget button text
            createBudgetBtn.text = "Edit Budget Plan"

            // Show/hide alert based on usage
            if (percentage >= 80) {
                alertCard?.visibility = View.VISIBLE
                alertText?.text = "You've used $percentage% of your ${budgetManager.getSchedule().lowercase()} budget"
            } else {
                alertCard?.visibility = View.GONE
            }
        } else {
            // Hide budget status if no budget set
            alertCard?.visibility = View.GONE
            createBudgetBtn.text = "Create a budget plan"
        }
    }

    private fun updateRecentTransactions() {
        val transactionsContainer = findViewById<LinearLayout>(R.id.transactionsContainer)
        val emptyTransactions = findViewById<LinearLayout>(R.id.emptyTransactions)
        
        val transactions = transactionManager.getRecentTransactions(3)
        
        // Clear existing transaction views (keep the header and empty state)
        transactionsContainer?.removeAllViews()
        
        if (transactions.isEmpty()) {
            emptyTransactions?.visibility = View.VISIBLE
        } else {
            emptyTransactions?.visibility = View.GONE
            
            transactions.forEachIndexed { index, transaction ->
                val transactionView = createTransactionView(transaction)
                transactionsContainer?.addView(transactionView)
                
                // Add divider between items (not after last)
                if (index < transactions.size - 1) {
                    val divider = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }
                        setBackgroundColor(Color.parseColor("#243B55"))
                    }
                    transactionsContainer?.addView(divider)
                }
            }
        }
    }

    private fun createTransactionView(transaction: Transaction): LinearLayout {
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 16)
        }

        // Icon
        val iconBg = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                marginEnd = 16
            }
            text = transaction.categoryIcon
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.progress_background)
        }
        container.addView(iconBg)

        // Details
        val detailsContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }

        val categoryText = TextView(this).apply {
            text = transaction.category
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        detailsContainer.addView(categoryText)

        val timeText = TextView(this).apply {
            text = formatTime(transaction.timestamp)
            textSize = 12f
            setTextColor(Color.parseColor("#B8C5D6"))
        }
        detailsContainer.addView(timeText)

        container.addView(detailsContainer)

        // Amount
        val amountText = TextView(this).apply {
            val prefix = if (transaction.type == "expense") "-" else "+"
            val color = if (transaction.type == "expense") "#EF5350" else "#66BB6A"
            text = "$prefix${formatCurrency(transaction.amount)}"
            textSize = 15f
            setTextColor(Color.parseColor(color))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        container.addView(amountText)

        return container
    }

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} min ago"
            diff < 86400000 -> {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Today, ${sdf.format(Date(timestamp))}"
            }
            diff < 172800000 -> {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Yesterday, ${sdf.format(Date(timestamp))}"
            }
            else -> {
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning!"
            hour < 17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        return format.format(amount).replace("PHP", "₱")
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            setHomeActive()
        }

        navTrack.setOnClickListener {
            startActivity(Intent(this, TrackActivity::class.java))
        }

        navAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", "expense")
            startActivity(intent)
        }

        navRecord.setOnClickListener {
            startActivity(Intent(this, RecordActivity::class.java))
        }
    }

    private fun setHomeActive() {
        navHome.setColorFilter(Color.WHITE)
        navTrack.setColorFilter(Color.parseColor("#8A9BAE"))
        navAdd.setColorFilter(Color.parseColor("#8A9BAE"))
        navRecord.setColorFilter(Color.parseColor("#8A9BAE"))
    }

    override fun onBackPressed() {
        showLogoutDialog()
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Do you want to logout?")

        builder.setPositiveButton("Yes") { dialog, which ->
            userManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }
}
