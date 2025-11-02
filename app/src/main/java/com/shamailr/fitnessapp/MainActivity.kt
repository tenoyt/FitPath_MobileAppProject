package com.shamailr.fitnessapp
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DashboardFragment()).commit()
        findViewById<Button>(R.id.btnDashboard).setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DashboardFragment()).commit()
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment()).commit()
        }
        findViewById<Button>(R.id.btnWorkouts).setOnClickListener {
            Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
