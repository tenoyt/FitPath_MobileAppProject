package com.example.fitpath

import  android.content.Context
import  android.content.SharedPreferences

class SessionManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Check if user is signed in
    fun isSignedIn(): Boolean {
        return prefs.getBoolean("signed_in", false)
    }

    // Mark user as signed in
    fun login() {
        prefs.edit().putBoolean("signed_in", true).apply()
    }

    // Mark user as signed out
    fun logout() {
        prefs.edit().putBoolean("signed_in", false).apply()
    }
}
