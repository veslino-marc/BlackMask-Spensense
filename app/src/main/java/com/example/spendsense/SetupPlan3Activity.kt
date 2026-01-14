package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class SetupPlan3Activity : AppCompatActivity() {
    
    private lateinit var schedule: String
    private var totalBudget: Double = 0.0
    private var fromRegistration: Boolean = false
    
    private lateinit var totalBudgetDisplay: TextView
    private lateinit var remainingDisplay: TextView
    private lateinit var needsInput: EditText
    private lateinit var savingsInput: EditText
    private lateinit var wantsInput: EditText
    private lateinit var budgetManager: BudgetManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_plan3)

        schedule = intent.getStringExtra("schedule") ?: "Monthly"
        totalBudget = intent.getDoubleExtra("budget", 0.0)
        fromRegistration = intent.getBooleanExtra("from_registration", false)
        
        budgetManager = BudgetManager(this)

        // Initialize views
        val backBtn: ImageView = findViewById(R.id.backBtn)
        val backButton: Button = findViewById(R.id.backButton)
        val finishBtn: Button = findViewById(R.id.finishBtn)
        
        totalBudgetDisplay = findViewById(R.id.totalBudgetDisplay)
        remainingDisplay = findViewById(R.id.remainingDisplay)
        needsInput = findViewById(R.id.needsInput)
        savingsInput = findViewById(R.id.savingsInput)
        wantsInput = findViewById(R.id.wantsInput)

        // Display total budget
        totalBudgetDisplay.text = formatCurrency(totalBudget)
        updateRemaining()

        // Pre-fill with suggested 50/20/30 split
        val suggestedNeeds = totalBudget * 0.50
        val suggestedSavings = totalBudget * 0.20
        val suggestedWants = totalBudget * 0.30
        
        needsInput.setText(String.format("%.2f", suggestedNeeds))
        savingsInput.setText(String.format("%.2f", suggestedSavings))
        wantsInput.setText(String.format("%.2f", suggestedWants))
        updateRemaining()

        // Add text watchers to update remaining
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateRemaining() }
        }
        
        needsInput.addTextChangedListener(textWatcher)
        savingsInput.addTextChangedListener(textWatcher)
        wantsInput.addTextChangedListener(textWatcher)

        // Back buttons
        backBtn.setOnClickListener { finish() }
        backButton.setOnClickListener { finish() }

        // Finish button
        finishBtn.setOnClickListener {
            val needs = needsInput.text.toString().toDoubleOrNull() ?: 0.0
            val savings = savingsInput.text.toString().toDoubleOrNull() ?: 0.0
            val wants = wantsInput.text.toString().toDoubleOrNull() ?: 0.0
            
            val total = needs + savings + wants
            
            if (total <= 0) {
                Toast.makeText(this, "Please allocate your budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (total > totalBudget) {
                Toast.makeText(this, "Allocation exceeds total budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save budget data
            budgetManager.saveBudget(schedule, totalBudget, needs, savings, wants)
            
            Toast.makeText(this, "Budget plan created! 🎉", Toast.LENGTH_SHORT).show()

            // Go to dashboard
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finishAffinity() // Clear all previous activities
        }
    }

    private fun updateRemaining() {
        val needs = needsInput.text.toString().toDoubleOrNull() ?: 0.0
        val savings = savingsInput.text.toString().toDoubleOrNull() ?: 0.0
        val wants = wantsInput.text.toString().toDoubleOrNull() ?: 0.0
        
        val allocated = needs + savings + wants
        val remaining = totalBudget - allocated
        
        remainingDisplay.text = "Remaining: ${formatCurrency(remaining)}"
        
        // Change color based on remaining
        if (remaining < 0) {
            remainingDisplay.setTextColor(resources.getColor(R.color.expense_red, null))
        } else if (remaining == 0.0) {
            remainingDisplay.setTextColor(resources.getColor(R.color.income_green, null))
        } else {
            remainingDisplay.setTextColor(resources.getColor(R.color.accent_blue, null))
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        return format.format(amount).replace("PHP", "₱")
    }
}
