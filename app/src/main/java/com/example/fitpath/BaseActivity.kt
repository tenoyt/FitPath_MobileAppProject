package com.example.fitpath

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    private var currentNightMode: Int = 0

    override fun onResume() {
        super.onResume()
        // Get the current night mode setting when the activity resumes
        currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val newNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // Check if the night mode has actually changed
        if (newNightMode != currentNightMode) {
            recreate()
        }
    }
}
