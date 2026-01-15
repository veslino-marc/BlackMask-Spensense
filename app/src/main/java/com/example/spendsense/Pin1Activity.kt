package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Pin1Activity : AppCompatActivity() {
    private var pinCode = StringBuilder()
    private lateinit var userManager: UserManager
    private lateinit var pinDots: Array<View>
    private lateinit var pinBoxes: Array<TextView>
    private var changePinOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin1)

        userManager = UserManager(this)
        changePinOnly = intent.getBooleanExtra("change_pin_only", false)

        // New dot-based UI
        pinDots = arrayOf(
            findViewById(R.id.pinDot1),
            findViewById(R.id.pinDot2),
            findViewById(R.id.pinDot3),
            findViewById(R.id.pinDot4)
        )

        // Keep pinBoxes for backward compatibility (hidden in layout)
        pinBoxes = arrayOf(
            findViewById(R.id.pinBox1),
            findViewById(R.id.pinBox2),
            findViewById(R.id.pinBox3),
            findViewById(R.id.pinBox4)
        )

        val continueBtn: Button = findViewById(R.id.continueBtn)
        continueBtn.setOnClickListener {
            if (pinCode.length == 4) {
                val intent = Intent(this, Pin2Activity::class.java)
                intent.putExtra("pin1", pinCode.toString())
                intent.putExtra("change_pin_only", changePinOnly)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter 4 digits", Toast.LENGTH_SHORT).show()
            }
        }

        setupNumberPad()
    }

    private fun setupNumberPad() {
        for (i in 0..9) {
            val resId = resources.getIdentifier("num$i", "id", packageName)
            if (resId != 0) {
                findViewById<Button>(resId).setOnClickListener { addDigit(i.toString()) }
            }
        }

        val backspaceId = resources.getIdentifier("backspace", "id", packageName)
        if (backspaceId != 0) {
            findViewById<Button>(backspaceId).setOnClickListener { removeDigit() }
        }
    }

    private fun addDigit(digit: String) {
        if (pinCode.length < 4) {
            pinCode.append(digit)
            updateDisplay()
        }
    }

    private fun removeDigit() {
        if (pinCode.isNotEmpty()) {
            pinCode.deleteCharAt(pinCode.length - 1)
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        for (i in 0..3) {
            if (i < pinCode.length) {
                pinDots[i].setBackgroundResource(R.drawable.pin_dot_filled)
            } else {
                pinDots[i].setBackgroundResource(R.drawable.pin_dot_empty)
            }
            // Keep pinBoxes updated for compatibility
            pinBoxes[i].text = if (i < pinCode.length) "●" else ""
        }
    }
}
