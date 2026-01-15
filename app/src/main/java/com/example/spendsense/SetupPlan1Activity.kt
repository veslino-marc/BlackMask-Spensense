package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SetupPlan1Activity : AppCompatActivity() {
    
    private var selectedSchedule: String = ""
    private var customDays: Int = 0
    private var fromRegistration: Boolean = false
    
    private lateinit var optionWeekly: LinearLayout
    private lateinit var optionBiWeekly: LinearLayout
    private lateinit var optionMonthly: LinearLayout
    private lateinit var optionCustom: LinearLayout
    
    private lateinit var checkWeekly: View
    private lateinit var checkBiWeekly: View
    private lateinit var checkMonthly: View
    private lateinit var checkCustom: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_plan1)

        fromRegistration = intent.getBooleanExtra("from_registration", false)

        // Initialize views
        optionWeekly = findViewById(R.id.optionWeekly)
        optionBiWeekly = findViewById(R.id.optionBiWeekly)
        optionMonthly = findViewById(R.id.optionMonthly)
        optionCustom = findViewById(R.id.optionCustom)
        
        checkWeekly = findViewById(R.id.checkWeekly)
        checkBiWeekly = findViewById(R.id.checkBiWeekly)
        checkMonthly = findViewById(R.id.checkMonthly)
        checkCustom = findViewById(R.id.checkCustom)

        val backBtn: ImageView = findViewById(R.id.backBtn)
        val nextBtn: Button = findViewById(R.id.nextBtn)

        // Back button - only allow going back if not from registration
        backBtn.setOnClickListener {
            if (!fromRegistration) {
                finish()
            }
        }
        
        // Hide back button during registration (can't go back)
        if (fromRegistration) {
            backBtn.visibility = View.INVISIBLE
        }

        // Schedule option clicks
        optionWeekly.setOnClickListener { selectSchedule("Weekly") }
        optionBiWeekly.setOnClickListener { selectSchedule("Bi-Weekly") }
        optionMonthly.setOnClickListener { selectSchedule("Monthly") }
        optionCustom.setOnClickListener { showCustomDaysDialog() }

        // Next button
        nextBtn.setOnClickListener {
            if (selectedSchedule.isEmpty()) {
                Toast.makeText(this, "Please select a schedule", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, SetupPlan2Activity::class.java)
                intent.putExtra("schedule", selectedSchedule)
                intent.putExtra("custom_days", customDays)
                intent.putExtra("from_registration", fromRegistration)
                startActivity(intent)
            }
        }
    }

    private fun showCustomDaysDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_days, null)
        val daysInput = dialogView.findViewById<EditText>(R.id.customDaysInput)

        // Pre-fill if already set
        if (customDays > 0) {
            daysInput.setText(customDays.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Custom Schedule")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val daysText = daysInput.text.toString()
                val days = daysText.toIntOrNull()

                if (days == null || days < 1 || days > 365) {
                    Toast.makeText(this, "Please enter a valid number (1-365 days)", Toast.LENGTH_SHORT).show()
                } else {
                    customDays = days
                    selectedSchedule = "Custom ($days days)"
                    selectScheduleUI("Custom")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun selectSchedule(schedule: String) {
        customDays = 0 // Reset custom days when selecting preset
        selectedSchedule = schedule
        selectScheduleUI(schedule)
    }

    private fun selectScheduleUI(schedule: String) {
        // Reset all options
        optionWeekly.setBackgroundResource(R.drawable.schedule_option_unselected)
        optionBiWeekly.setBackgroundResource(R.drawable.schedule_option_unselected)
        optionMonthly.setBackgroundResource(R.drawable.schedule_option_unselected)
        optionCustom.setBackgroundResource(R.drawable.schedule_option_unselected)
        
        checkWeekly.setBackgroundResource(R.drawable.pin_dot_empty)
        checkBiWeekly.setBackgroundResource(R.drawable.pin_dot_empty)
        checkMonthly.setBackgroundResource(R.drawable.pin_dot_empty)
        checkCustom.setBackgroundResource(R.drawable.pin_dot_empty)

        // Highlight selected option
        when (schedule) {
            "Weekly" -> {
                optionWeekly.setBackgroundResource(R.drawable.schedule_option_selected)
                checkWeekly.setBackgroundResource(R.drawable.pin_dot_filled)
            }
            "Bi-Weekly" -> {
                optionBiWeekly.setBackgroundResource(R.drawable.schedule_option_selected)
                checkBiWeekly.setBackgroundResource(R.drawable.pin_dot_filled)
            }
            "Monthly" -> {
                optionMonthly.setBackgroundResource(R.drawable.schedule_option_selected)
                checkMonthly.setBackgroundResource(R.drawable.pin_dot_filled)
            }
            "Custom" -> {
                optionCustom.setBackgroundResource(R.drawable.schedule_option_selected)
                checkCustom.setBackgroundResource(R.drawable.pin_dot_filled)
            }
        }
    }
}
