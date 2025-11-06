package com.example.fitpath
import android.os.Bundle
import android.widget.Button
import androidx.navigation.findNavController

class MainnActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainn)
        supportActionBar?.hide()


        findViewById<Button>(R.id.btnDashboard).setOnClickListener {
            findNavController(R.id.fragment_container)
                .navigate(R.id.Dashboard)
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            findNavController(R.id.fragment_container)
                .navigate(R.id.settingsFragment)
        }
        findViewById<Button>(R.id.btnWorkouts).setOnClickListener {
            findNavController(R.id.fragment_container)
                .navigate(R.id.workoutLibraryFragment)
        }


    }
}