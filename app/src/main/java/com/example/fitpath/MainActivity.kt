package com.example.fitpath

import android.os.Bundle
import android.view.View // Import the View class
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

// Inherit from BaseActivity for theme handling
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment

        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setupWithNavController(navController)

        // Add a listener to control BottomNavigationView visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Add all destinations where the bottom nav should be hidden
                R.id.Login,
                R.id.Register -> {
                    bottomNavigationView.visibility = View.GONE
                }
                // For all other destinations, make it visible
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}
