package com.example.spendsense

import android.content.Context
import android.content.SharedPreferences

class BudgetManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_HAS_BUDGET = "has_budget"
        private const val KEY_SCHEDULE = "schedule"
        private const val KEY_CUSTOM_DAYS = "custom_days"
        private const val KEY_TOTAL_BUDGET = "total_budget"
        private const val KEY_NEEDS = "needs"
        private const val KEY_SAVINGS = "savings"
        private const val KEY_WANTS = "wants"
        private const val KEY_SPENT = "spent"
    }

    fun saveBudget(schedule: String, totalBudget: Double, needs: Double, savings: Double, wants: Double, customDays: Int = 0) {
        prefs.edit().apply {
            putBoolean(KEY_HAS_BUDGET, true)
            putString(KEY_SCHEDULE, schedule)
            putInt(KEY_CUSTOM_DAYS, customDays)
            putFloat(KEY_TOTAL_BUDGET, totalBudget.toFloat())
            putFloat(KEY_NEEDS, needs.toFloat())
            putFloat(KEY_SAVINGS, savings.toFloat())
            putFloat(KEY_WANTS, wants.toFloat())
            putFloat(KEY_SPENT, 0f) // Reset spent when creating new budget
            apply()
        }
    }

    fun hasBudget(): Boolean {
        return prefs.getBoolean(KEY_HAS_BUDGET, false)
    }

    fun getSchedule(): String {
        return prefs.getString(KEY_SCHEDULE, "Monthly") ?: "Monthly"
    }

    fun getCustomDays(): Int {
        return prefs.getInt(KEY_CUSTOM_DAYS, 0)
    }

    fun getTotalBudget(): Double {
        return prefs.getFloat(KEY_TOTAL_BUDGET, 0f).toDouble()
    }

    fun getNeeds(): Double {
        return prefs.getFloat(KEY_NEEDS, 0f).toDouble()
    }

    fun getSavings(): Double {
        return prefs.getFloat(KEY_SAVINGS, 0f).toDouble()
    }

    fun getWants(): Double {
        return prefs.getFloat(KEY_WANTS, 0f).toDouble()
    }

    fun getSpent(): Double {
        return prefs.getFloat(KEY_SPENT, 0f).toDouble()
    }

    fun addExpense(amount: Double) {
        val currentSpent = getSpent()
        prefs.edit().putFloat(KEY_SPENT, (currentSpent + amount).toFloat()).apply()
    }

    fun setSpent(amount: Double) {
        prefs.edit().putFloat(KEY_SPENT, amount.toFloat()).apply()
    }

    fun getRemaining(): Double {
        return getTotalBudget() - getSpent()
    }

    fun getUsagePercentage(): Int {
        val total = getTotalBudget()
        if (total <= 0) return 0
        return ((getSpent() / total) * 100).toInt().coerceIn(0, 100)
    }

    fun clearBudget() {
        prefs.edit().clear().apply()
    }
}
