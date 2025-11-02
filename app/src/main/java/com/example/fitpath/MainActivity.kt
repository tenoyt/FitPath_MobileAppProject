package com.example.fitpath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.fitpath.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation controller for fragments
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // No ActionBar setup needed since we are using NoActionBar theme
    }
}

