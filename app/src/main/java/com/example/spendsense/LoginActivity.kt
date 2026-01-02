package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

        if (userManager.isLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        togglePassword = findViewById(R.id.togglePassword)

        // Password visibility toggle
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT
                togglePassword.setImageResource(android.R.drawable.ic_menu_view)
            } else {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            passwordField.setSelection(passwordField.text.length)
        }

        loginBtn.setOnClickListener { loginUser() }
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

                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}