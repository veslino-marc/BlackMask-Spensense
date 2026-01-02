// FILE: DashboardActivity.kt (UPDATED - WITH PIN CHECK ON RESTART)
package com.example.spendsense

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class DashboardActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var navHome: ImageView
    private lateinit var navTrack: ImageView
    private lateinit var navAdd: ImageView
    private lateinit var navRecord: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userManager = UserManager(this)

        // If not logged in, go to Login
        if (!userManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Check if coming from login/register flow (flag from Pin3Activity or LoginActivity)
        val fromLogin = intent.getBooleanExtra("from_login", false)

        // If NOT from login, ask for PIN (app restart scenario)
        if (!fromLogin) {
            startActivity(Intent(this, PINVerifyActivity::class.java))
            finish()
            return
        }

        // Set username
        val greeting: TextView = findViewById(R.id.greeting)
        val username: TextView = findViewById(R.id.username)

        greeting.text = "Good Day!"
        val displayName = userManager.getUsername()
        username.text = "Hello, $displayName"

        // Initialize navigation icons
        navHome = findViewById(R.id.navHome)
        navTrack = findViewById(R.id.navTrack)
        navAdd = findViewById(R.id.navAdd)
        navRecord = findViewById(R.id.navRecord)

        // Set home as active
        setHomeActive()

        // Setup navigation
        setupNavigation()

        // Action buttons
        val addExpenseBtn: Button = findViewById(R.id.addExpenseBtn)
        val addCashBtn: Button = findViewById(R.id.addCashBtn)
        val profileIcon: ImageView = findViewById(R.id.profileIcon)

        addExpenseBtn.setOnClickListener {
            Toast.makeText(this, "Add Expense - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        addCashBtn.setOnClickListener {
            Toast.makeText(this, "Add Cash - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        profileIcon.setOnClickListener {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            setHomeActive()
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        navTrack.setOnClickListener {
            Toast.makeText(this, "Track - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        navAdd.setOnClickListener {
            Toast.makeText(this, "Add - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        navRecord.setOnClickListener {
            Toast.makeText(this, "Record - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setHomeActive() {
        navHome.setColorFilter(Color.BLACK)
        navTrack.setColorFilter(Color.GRAY)
        navAdd.setColorFilter(Color.GRAY)
        navRecord.setColorFilter(Color.GRAY)
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