package com.example.fitpath

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class FitnessApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Get the saved dark mode preference
        val prefs = Prefs(this)
        val useDarkMode = prefs.darkMode

        // Set the default theme for the entire app when it starts
        if (useDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
