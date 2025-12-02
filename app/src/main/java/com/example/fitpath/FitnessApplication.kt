package com.example.fitpath

import android.app.Application
import android.app.NotificationChannel // <-- Their import
import android.app.NotificationManager // <-- Their import
import android.os.Build // <-- Their import
import androidx.appcompat.app.AppCompatDelegate

class FitnessApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = Prefs(this)
        val useDark = prefs.darkMode
        if (useDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fitpath_reminders",
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Daily reminder notifications"

            // Register the channel with the system
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
