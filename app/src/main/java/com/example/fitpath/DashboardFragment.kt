package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton // <-- Your profile button import
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth // <-- Your Firebase Auth
import com.google.firebase.firestore.FirebaseFirestore // <-- Your Firestore
import com.google.android.gms.maps.CameraUpdateFactory // <-- Their Map Imports
import com.google.android.gms.maps.GoogleMap // <-- Their Map Imports
import com.google.android.gms.maps.OnMapReadyCallback // <-- Their Map Imports
import com.google.android.gms.maps.SupportMapFragment // <-- Their Map Imports
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.fitpath.R

// IMPORTANT: Implement OnMapReadyCallback for the map feature
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
        // Find the map fragment and initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Assuming layout has cardMapView for the map container
        view.findViewById<View>(R.id.cardMapView)?.visibility = View.VISIBLE

        // --- CARD LISTENERS ---
        view.findViewById<MaterialCardView>(R.id.cardLogWorkout).setOnClickListener {
            // Note: This needs to navigate to R.id.workoutBuilderFragment via Nav Graph
            Toast.makeText(requireContext(), "Workout logging coming soon!", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<MaterialCardView>(R.id.cardLogMeal).setOnClickListener {
            Toast.makeText( requireContext(), "Meal logging coming soon!", Toast.LENGTH_SHORT).show()
        }

        //community card
        view.findViewById<MaterialCardView>(R.id.cardCommunity).setOnClickListener {
            findNavController().navigate(R.id.communityFragment)

        }


        // --- AUTH LISTENERS ---
        loginBtn.setOnClickListener {
            findNavController().navigate(R.id.Login)
        }

        signOutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // --- PROFILE BUTTON (Synchronization Fix) ---
        profileBtn.setOnClickListener {
            // Tell the BottomNav to select the 'settings' tab for sync
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.settings
        }
    }

    // --- MAP IMPLEMENTATION ---
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Example: Add a marker
        val defaultLocation = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Start Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Optional: enable zoom controls
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