package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Pin2Activity : AppCompatActivity() {
    private var pinCode = StringBuilder()
    private lateinit var pin1: String
    private lateinit var pinBoxes: Array<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin2)

        pin1 = intent.getStringExtra("pin1") ?: ""

        pinBoxes = arrayOf(
            findViewById(R.id.pinBox1),
            findViewById(R.id.pinBox2),
            findViewById(R.id.pinBox3),
            findViewById(R.id.pinBox4)
        )

        val verifyBtn: Button = findViewById(R.id.verifyBtn)
        verifyBtn.setOnClickListener {
            if (pinCode.length == 4) {
                if (pinCode.toString() == pin1) {
                    val intent = Intent(this, Pin3Activity::class.java)
                    intent.putExtra("pin", pin1)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                    pinCode.setLength(0)
                    updateDisplay()
                }
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
            pinBoxes[i].text = if (i < pinCode.length) "●" else ""
        }
    }
}