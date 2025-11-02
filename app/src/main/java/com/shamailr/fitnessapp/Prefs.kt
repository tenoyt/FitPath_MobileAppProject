package com.shamailr.fitnessapp
import android.content.Context

class Prefs(ctx: Context) {
    private val p = ctx.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    var darkMode: Boolean
        get() = p.getBoolean("dark_mode", false)
        set(v) { p.edit().putBoolean("dark_mode", v).apply() }
}
