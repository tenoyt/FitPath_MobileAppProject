package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton // <-- Make sure this is imported
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // --- Declare all your UI elements ---
    private lateinit var loginBtn: Button
    private lateinit var welcomeText: TextView
    private lateinit var signOutBtn: Button
    private lateinit var profileBtn: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- INITIALIZE FIREBASE ---
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // --- FIND ALL UI VIEWS from your new XML ---
        loginBtn = view.findViewById(R.id.btnLoginRegister)
        welcomeText = view.findViewById(R.id.tvWelcome)
        signOutBtn = view.findViewById(R.id.btnSignOut)
        profileBtn = view.findViewById(R.id.btn_profile)

        // (Your card listeners)
        view.findViewById<MaterialCardView>(R.id.cardLogWorkout).setOnClickListener {
            // TODO: You will update this to navigate to the workout logger
            Toast.makeText(requireContext(), "Workout logging coming soon!", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<MaterialCardView>(R.id.cardLogMeal).setOnClickListener {
            // TODO: You will update this to navigate to the meal logger
            Toast.makeText(requireContext(), "Meal logging coming soon!", Toast.LENGTH_SHORT).show()
        }

        // --- SET UP LISTENERS (They never change) ---
        loginBtn.setOnClickListener {
            findNavController().navigate(R.id.Login)
        }

        signOutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        profileBtn.setOnClickListener {
            // Find the BottomNavigationView in the parent Activity
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

            // Tell the BottomNav to select the 'settings' tab
            bottomNav?.selectedItemId = R.id.settings
        }
    }

    // onStart() runs every time the fragment becomes visible
    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun updateUI() {
        val currentUser = auth.currentUser

        welcomeText.visibility = View.VISIBLE

        if (currentUser != null) {
            // --- User IS Logged In ---
            loginBtn.visibility = View.GONE
            signOutBtn.visibility = View.VISIBLE
            profileBtn.visibility = View.VISIBLE

            fetchUsername(currentUser.uid)
        } else {
            // --- User is NOT Logged In ---
            loginBtn.visibility = View.VISIBLE
            signOutBtn.visibility = View.GONE
            profileBtn.visibility = View.GONE

            welcomeText.text = "Welcome Back"
        }
    }

    private fun fetchUsername(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    if (!username.isNullOrEmpty()) {
                        welcomeText.text = "Welcome, $username"
                    } else {
                        Log.w("DashboardFragment", "Username field is null or empty for user $uid")
                        welcomeText.text = "Welcome!"
                    }
                } else {
                    Log.w("DashboardFragment", "No user document found for $uid")
                    welcomeText.text = "Welcome!"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DashboardFragment", "Error fetching username: ", exception)
                welcomeText.text = "Welcome!"
            }
    }
}