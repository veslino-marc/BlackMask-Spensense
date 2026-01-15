package com.example.spendsense

import android.content.Context
import android.content.SharedPreferences

class UserManager(context: Context) {
    companion object {
        private const val PREF_NAME = "SpendSensePrefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_PIN = "pin"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun registerUser(username: String, email: String, password: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun updateProfile(username: String, email: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun updatePassword(newPassword: String) {
        prefs.edit()
            .putString(KEY_PASSWORD, newPassword)
            .apply()
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun setLoggedIn(username: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String {
        return prefs.getString(KEY_USERNAME, "User") ?: "User"
    }

    fun getEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun getPassword(): String {
        return prefs.getString(KEY_PASSWORD, "") ?: ""
    }

    fun getPin(): String {
        return prefs.getString(KEY_PIN, "") ?: ""
    }

    fun userExists(email: String): Boolean {
        return prefs.getString(KEY_EMAIL, "") == email
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}