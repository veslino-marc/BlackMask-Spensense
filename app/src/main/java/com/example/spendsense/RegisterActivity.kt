package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var registerBtn: Button
    private lateinit var loginLink: TextView
    private lateinit var userManager: UserManager
    private lateinit var transactionManager: TransactionManager
    private lateinit var budgetManager: BudgetManager
    private lateinit var togglePassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userManager = UserManager(this)
        transactionManager = TransactionManager(this)
        budgetManager = BudgetManager(this)

        usernameField = findViewById(R.id.username)
        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        confirmPasswordField = findViewById(R.id.confirmPassword)
        registerBtn = findViewById(R.id.registerBtn)
        loginLink = findViewById(R.id.loginLink)
        togglePassword = findViewById(R.id.togglePassword)
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword)

        // Setup password wrapper focus handling
        val passwordWrapper: LinearLayout = findViewById(R.id.passwordWrapper)
        val confirmPasswordWrapper: LinearLayout = findViewById(R.id.confirmPasswordWrapper)
        
        passwordField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordWrapper.setBackgroundResource(R.drawable.input_background_focused)
            } else {
                passwordWrapper.setBackgroundResource(R.drawable.input_background_unfocused)
            }
        }
        
        confirmPasswordField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                confirmPasswordWrapper.setBackgroundResource(R.drawable.input_background_focused)
            } else {
                confirmPasswordWrapper.setBackgroundResource(R.drawable.input_background_unfocused)
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

        // Confirm password visibility toggle
        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                confirmPasswordField.inputType = InputType.TYPE_CLASS_TEXT
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                confirmPasswordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_closed)
            }
            confirmPasswordField.setSelection(confirmPasswordField.text.length)
        }

        registerBtn.setOnClickListener { registerUser() }
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val username = usernameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()
        val confirmPassword = confirmPasswordField.text.toString()

        when {
            username.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Clear old user data before registering new user
                transactionManager.clearAllTransactions()
                budgetManager.clearBudget()
                
                userManager.registerUser(username, email, password)
                Toast.makeText(this, "Registration successful! Set your PIN.", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, Pin1Activity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                finish()
            }
        }
    }
}
