package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var onboardingManager: OnboardingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userManager = UserManager(this)
        onboardingManager = OnboardingManager(this)

        // Animate logo
        val logoContainer: LinearLayout = findViewById(R.id.logoContainer)
        val loadingIndicator: View = findViewById(R.id.loadingIndicator)

        // Fade in animation
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
        }

        // Scale animation
        val scaleUp = ScaleAnimation(
            0.8f, 1f, 0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 800
            fillAfter = true
        }

        val animSet = AnimationSet(true).apply {
            addAnimation(fadeIn)
            addAnimation(scaleUp)
        }

        logoContainer.startAnimation(animSet)

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2500)
    }

    private fun navigateToNextScreen() {
        val intent = when {
            // First time user - show onboarding
            !onboardingManager.hasCompletedOnboarding() -> {
                Intent(this, OnboardingActivity::class.java)
            }
            // Returning user who is logged in - verify PIN
            userManager.isLoggedIn() -> {
                Intent(this, PINVerifyActivity::class.java)
            }
            // Returning user not logged in - show login
            else -> {
                Intent(this, LoginActivity::class.java)
            }
        }

        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
