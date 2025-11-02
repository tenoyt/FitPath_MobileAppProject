package com.shamailr.fitnessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the cards by their new IDs and set click listeners
        view.findViewById<MaterialCardView>(R.id.cardLogWorkout).setOnClickListener {
            Toast.makeText(requireContext(), "Workout logging coming soon!", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialCardView>(R.id.cardLogMeal).setOnClickListener {
            Toast.makeText(requireContext(), "Meal logging coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}

