package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsContainer: LinearLayout
    private lateinit var nextBtn: Button
    private lateinit var skipBtn: TextView
    private lateinit var onboardingManager: OnboardingManager

    private val slides = listOf(
        OnboardingSlide(
            icon = "💰",
            title = "Track Your Spending",
            description = "Keep track of every expense and income with ease. Know exactly where your money goes."
        ),
        OnboardingSlide(
            icon = "📊",
            title = "Set Budget Goals",
            description = "Create personalized budgets for needs, savings, and wants. Stay on top of your financial goals."
        ),
        OnboardingSlide(
            icon = "✨",
            title = "Stay in Control",
            description = "Get insights and alerts to help you make smarter financial decisions every day."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        onboardingManager = OnboardingManager(this)

        viewPager = findViewById(R.id.viewPager)
        dotsContainer = findViewById(R.id.dotsContainer)
        nextBtn = findViewById(R.id.nextBtn)
        skipBtn = findViewById(R.id.skipBtn)

        // Setup ViewPager
        val adapter = OnboardingAdapter(slides)
        viewPager.adapter = adapter

        // Setup dots
        setupDots()
        updateDots(0)

        // Page change listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                updateButton(position)
            }
        })

        // Button clicks
        nextBtn.setOnClickListener {
            if (viewPager.currentItem < slides.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                finishOnboarding()
            }
        }

        skipBtn.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun setupDots() {
        dotsContainer.removeAllViews()
        for (i in slides.indices) {
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(10, 10).apply {
                    marginStart = 8
                    marginEnd = 8
                }
                setBackgroundResource(R.drawable.dot_inactive)
            }
            dotsContainer.addView(dot)
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until dotsContainer.childCount) {
            val dot = dotsContainer.getChildAt(i)
            dot.setBackgroundResource(
                if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }

    private fun updateButton(position: Int) {
        if (position == slides.size - 1) {
            nextBtn.text = "Get Started"
            skipBtn.visibility = View.INVISIBLE
        } else {
            nextBtn.text = "Next"
            skipBtn.visibility = View.VISIBLE
        }
    }

    private fun finishOnboarding() {
        onboardingManager.setOnboardingCompleted()
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        } else {
            super.onBackPressed()
        }
    }
}

// Data class for slides
data class OnboardingSlide(
    val icon: String,
    val title: String,
    val description: String
)

// ViewPager Adapter
class OnboardingAdapter(private val slides: List<OnboardingSlide>) :
    RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: TextView = view.findViewById(R.id.slideIcon)
        val title: TextView = view.findViewById(R.id.slideTitle)
        val description: TextView = view.findViewById(R.id.slideDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_slide, parent, false)
        return SlideViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        val slide = slides[position]
        holder.icon.text = slide.icon
        holder.title.text = slide.title
        holder.description.text = slide.description
    }

    override fun getItemCount() = slides.size
}
