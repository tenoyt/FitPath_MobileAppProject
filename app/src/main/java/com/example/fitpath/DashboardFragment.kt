package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DashboardFragment : Fragment(R.layout.fragment_dashboard), OnMapReadyCallback {

    // --- AUTHENTICATION VARIABLES ---
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // --- UI ELEMENTS ---
    private lateinit var loginBtn: Button
    private lateinit var welcomeText: TextView
    private lateinit var signOutBtn: Button
    private lateinit var profileBtn: ImageButton

    // --- MAP VARIABLE ---
    private lateinit var mMap: GoogleMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- INITIALIZE FIREBASE ---
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // --- FIND ALL UI VIEWS ---
        loginBtn = view.findViewById(R.id.btnLoginRegister)
        welcomeText = view.findViewById(R.id.tvWelcome)
        signOutBtn = view.findViewById(R.id.btnSignOut)
        profileBtn = view.findViewById(R.id.btn_profile)

        // --- MAP INITIALIZATION ---
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        view.findViewById<View>(R.id.cardMapView)?.visibility = View.VISIBLE

        // --- CARD LISTENERS ---

        // 1. Log Workout -> Goes to Workout Library
        view.findViewById<MaterialCardView>(R.id.cardLogWorkout).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_workoutLibraryFragment)
        }

        // 2. Log Meal -> Placeholder
        view.findViewById<MaterialCardView>(R.id.cardLogMeal).setOnClickListener {
            Toast.makeText(requireContext(), "Meal logging coming soon!", Toast.LENGTH_SHORT).show()
        }

        // 3. Community -> Goes to Community Fragment
        view.findViewById<MaterialCardView>(R.id.cardCommunity).setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }

        // --- AUTH LISTENERS ---
        loginBtn.setOnClickListener {
            // Ensure this ID matches your nav_graph.xml destination for Login
            findNavController().navigate(R.id.Login)
        }

        signOutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // --- PROFILE BUTTON ---
        // Switches the Bottom Navigation tab to "Settings"
        profileBtn.setOnClickListener {
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.settings
        }
    }

    // --- MAP IMPLEMENTATION ---
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Example: Add a marker for a default location (e.g., Sydney)
        val defaultLocation = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Start Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        mMap.uiSettings.isZoomControlsEnabled = true
    }

    // --- UI STATE HANDLER ---
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
                        welcomeText.text = "Welcome!"
                    }
                } else {
                    welcomeText.text = "Welcome!"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DashboardFragment", "Error fetching username: ", exception)
                welcomeText.text = "Welcome!"
            }
    }
}
