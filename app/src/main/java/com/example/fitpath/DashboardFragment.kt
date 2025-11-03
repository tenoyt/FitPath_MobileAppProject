package com.example.fitpath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var loginBtn: Button

    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        loginBtn = view.findViewById(R.id.btnLoginRegister)

        // Workout and Meal cards
        view.findViewById<MaterialCardView>(R.id.cardLogWorkout).setOnClickListener {
            Toast.makeText(requireContext(), "Workout logging coming soon!", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<MaterialCardView>(R.id.cardLogMeal).setOnClickListener {
            Toast.makeText(requireContext(), "Meal logging coming soon!", Toast.LENGTH_SHORT).show()
        }



        updateLoginButton()
    }

    private fun updateLoginButton() {
        if (sessionManager.isSignedIn()) {
            loginBtn.text = "Sign Out"
            loginBtn.setOnClickListener {
                sessionManager.logout()
                Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
                updateLoginButton()
                findNavController().navigate(R.id.Login)
            }
        } else {
            loginBtn.text = "Login / Register"
            loginBtn.setOnClickListener {
                findNavController().navigate(R.id.Login)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateLoginButton()
    }
}
