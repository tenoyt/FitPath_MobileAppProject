package com.example.fitpath

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val store by lazy { requireContext().getSharedPreferences("dashboard", android.content.Context.MODE_PRIVATE) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSteps: TextView = view.findViewById(R.id.tvSteps)
        val tvWorkouts: TextView = view.findViewById(R.id.tvWorkouts)
        val tvGoal: TextView = view.findViewById(R.id.tvGoal)
        val btnLogWorkout: Button = view.findViewById(R.id.btnLogWorkout)
        val btnWorkoutLibrary: Button = view.findViewById(R.id.btnWorkoutLibrary)
        val btnSettings: Button = view.findViewById(R.id.btnSettings)

        val settings = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val name = settings.getString("name", "Athlete")
        tvName.text = "Hi, " + name

        updateStats(tvSteps, tvWorkouts, tvGoal)

        btnLogWorkout.setOnClickListener {
            val workouts = store.getInt("workouts_week", 0) + 1
            store.edit().putInt("workouts_week", workouts).apply()
            updateStats(tvSteps, tvWorkouts, tvGoal)
        }

        btnWorkoutLibrary.setOnClickListener {
            findNavController().navigate(R.id.workoutLibrary)
        }

        btnSettings.setOnClickListener {
            findNavController().navigate(R.id.settings)
        }
    }

    private fun updateStats(tvSteps: TextView, tvWorkouts: TextView, tvGoal: TextView) {
        val steps = store.getInt("steps_today", 4500)
        val workouts = store.getInt("workouts_week", 0)
        val goal = store.getInt("goal_weekly", 3)
        tvSteps.text = steps.toString()
        tvWorkouts.text = workouts.toString()
        tvGoal.text = "Weekly goal: " + workouts + "/" + goal
    }
}