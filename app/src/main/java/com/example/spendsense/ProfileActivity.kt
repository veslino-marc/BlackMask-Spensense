package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var budgetManager: BudgetManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userManager = UserManager(this)
        budgetManager = BudgetManager(this)

        setupViews()
        loadUserData()
        loadBudgetData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadBudgetData()
    }

    private fun setupViews() {
        val backBtn: ImageView = findViewById(R.id.backBtn)
        val editProfileBtn: LinearLayout = findViewById(R.id.editProfileBtn)
        val changePinBtn: LinearLayout = findViewById(R.id.changePinBtn)
        val changePasswordBtn: LinearLayout = findViewById(R.id.changePasswordBtn)
        val resetBudgetBtn: LinearLayout = findViewById(R.id.resetBudgetBtn)
        val aboutBtn: LinearLayout = findViewById(R.id.aboutBtn)
        val logoutBtn: Button = findViewById(R.id.logoutBtn)

        backBtn.setOnClickListener { finish() }

        editProfileBtn.setOnClickListener {
            showEditProfileDialog()
        }

        changePinBtn.setOnClickListener {
            showChangePinDialog()
        }

        changePasswordBtn.setOnClickListener {
            showChangePasswordDialog()
        }

        resetBudgetBtn.setOnClickListener {
            showResetBudgetDialog()
        }

        aboutBtn.setOnClickListener {
            showAboutDialog()
        }

        logoutBtn.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadUserData() {
        val avatarInitial: TextView = findViewById(R.id.avatarInitial)
        val profileName: TextView = findViewById(R.id.profileName)
        val profileEmail: TextView = findViewById(R.id.profileEmail)
        val memberSince: TextView = findViewById(R.id.memberSince)

        val username = userManager.getUsername()
        val email = userManager.getEmail()

        // Set avatar initial
        avatarInitial.text = if (username.isNotEmpty()) {
            username.first().uppercaseChar().toString()
        } else {
            "U"
        }

        profileName.text = username
        profileEmail.text = email

        // Member since (using current year as placeholder)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        memberSince.text = "Member since $year"
    }

    private fun loadBudgetData() {
        val totalBudgetValue: TextView = findViewById(R.id.totalBudgetValue)
        val spentValue: TextView = findViewById(R.id.spentValue)
        val remainingValue: TextView = findViewById(R.id.remainingValue)

        if (budgetManager.hasBudget()) {
            totalBudgetValue.text = formatCurrency(budgetManager.getTotalBudget())
            spentValue.text = formatCurrency(budgetManager.getSpent())
            remainingValue.text = formatCurrency(budgetManager.getRemaining())
        } else {
            totalBudgetValue.text = "₱0"
            spentValue.text = "₱0"
            remainingValue.text = "₱0"
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val usernameInput = dialogView.findViewById<EditText>(R.id.editUsername)
        val emailInput = dialogView.findViewById<EditText>(R.id.editEmail)

        // Pre-fill current values
        usernameInput.setText(userManager.getUsername())
        emailInput.setText(userManager.getEmail())

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = usernameInput.text.toString().trim()
                val newEmail = emailInput.text.toString().trim()

                if (newUsername.isEmpty()) {
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Update user data
                userManager.updateProfile(newUsername, newEmail)
                loadUserData()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePinDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change PIN")
            .setMessage("You will be redirected to create a new PIN. Continue?")
            .setPositiveButton("Yes") { _, _ ->
                // Redirect to PIN setup with flag indicating it's just a PIN change
                val intent = Intent(this, Pin1Activity::class.java)
                intent.putExtra("change_pin_only", true)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = dialogView.findViewById<EditText>(R.id.currentPassword)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPassword)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmNewPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                when {
                    currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                    currentPassword != userManager.getPassword() -> {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                    newPassword == currentPassword -> {
                        Toast.makeText(this, "New password must be different from current", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        userManager.updatePassword(newPassword)
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetBudgetDialog() {
        if (!budgetManager.hasBudget()) {
            Toast.makeText(this, "No budget to reset", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Budget")
            .setMessage("This will delete your current budget plan. You'll need to create a new one. Are you sure?")
            .setPositiveButton("Reset") { _, _ ->
                budgetManager.clearBudget()
                loadBudgetData()
                Toast.makeText(this, "Budget has been reset", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About SpendSense")
            .setMessage(
                "SpendSense v1.0.0\n\n" +
                "Your personal finance companion.\n\n" +
                "Track your expenses, manage your budget, and take control of your financial future.\n\n" +
                "© 2026 SpendSense"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                userManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        return format.format(amount).replace("PHP", "₱")
    }
}
