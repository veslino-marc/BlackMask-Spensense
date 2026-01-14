package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetupPlan2Activity : AppCompatActivity() {
    
    private lateinit var schedule: String
    private var fromRegistration: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_plan2)

        schedule = intent.getStringExtra("schedule") ?: "Monthly"
        fromRegistration = intent.getBooleanExtra("from_registration", false)

        val backBtn: ImageView = findViewById(R.id.backBtn)
        val backButton: Button = findViewById(R.id.backButton)
        val nextBtn: Button = findViewById(R.id.nextBtn)
        val budgetInput: EditText = findViewById(R.id.budgetInput)
        val scheduleLabel: TextView = findViewById(R.id.scheduleLabel)

        // Update schedule label
        scheduleLabel.text = "$schedule Budget"

        // Back buttons
        backBtn.setOnClickListener { finish() }
        backButton.setOnClickListener { finish() }

        // Next button
        nextBtn.setOnClickListener {
            val budgetText = budgetInput.text.toString()
            
            if (budgetText.isEmpty()) {
                Toast.makeText(this, "Please enter your budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetText.toDoubleOrNull()
            if (budget == null || budget <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SetupPlan3Activity::class.java)
            intent.putExtra("schedule", schedule)
            intent.putExtra("budget", budget)
            intent.putExtra("from_registration", fromRegistration)
            startActivity(intent)
        }
    }
}
