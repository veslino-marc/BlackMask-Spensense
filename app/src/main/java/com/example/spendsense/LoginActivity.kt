package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginBtn: Button
    private lateinit var userManager: UserManager
    private lateinit var togglePassword: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userManager = UserManager(this)

        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        togglePassword = findViewById(R.id.togglePassword)

        // Setup password wrapper focus handling
        val passwordWrapper: LinearLayout = findViewById(R.id.passwordWrapper)
        passwordField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordWrapper.setBackgroundResource(R.drawable.input_background_focused)
            } else {
                passwordWrapper.setBackgroundResource(R.drawable.input_background_unfocused)
            }
        }

        // Password visibility toggle
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT
                togglePassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_closed)
            }
            passwordField.setSelection(passwordField.text.length)
        }

        loginBtn.setOnClickListener { loginUser() }

        // Forgot password link
        val forgotPasswordLink: TextView = findViewById(R.id.forgotPasswordLink)
        forgotPasswordLink.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Register link
        val registerLink: TextView = findViewById(R.id.registerLink)
        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.resetEmail)

        AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setView(dialogView)
            .setPositiveButton("Continue") { _, _ ->
                val email = emailInput.text.toString().trim()
                
                if (email.isEmpty()) {
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val storedEmail = userManager.getEmail()
                
                if (email == storedEmail) {
                    // Email matches, show reset password dialog
                    showResetPasswordDialog()
                } else {
                    Toast.makeText(this, "Email not found. Please check and try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPassword)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmNewPassword)

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Reset") { _, _ ->
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                when {
                    newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Update password
                        userManager.updatePassword(newPassword)
                        Toast.makeText(this, "Password reset successfully! Please login.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loginUser() {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()

        when {
            email.isEmpty() || password.isEmpty() -> {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val storedEmail = userManager.getEmail()
                val storedPassword = userManager.getPassword()
                val storedUsername = userManager.getUsername()

                if (email == storedEmail && password == storedPassword) {
                    userManager.setLoggedIn(storedUsername)
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Go to PIN verification for security
                    val intent = Intent(this, PINVerifyActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}