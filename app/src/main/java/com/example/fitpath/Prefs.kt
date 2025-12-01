package com.example.fitpath
import android.content.Context

class Prefs(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    var darkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(v) { prefs.edit().putBoolean("dark_mode", v).apply() }

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reminders_enabled", enabled).apply()
    }

    fun getRemindersEnabled(): Boolean {
        return prefs.getBoolean("reminders_enabled", false)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt("reminder_hour", hour)
            .putInt("reminder_minute", minute)
            .apply()
    }

    fun getReminderHour(): Int {
        return prefs.getInt("reminder_hour", 9) // Default 9 AM
    }

    fun getReminderMinute(): Int {
        return prefs.getInt("reminder_minute", 0) // Default :00
    }}