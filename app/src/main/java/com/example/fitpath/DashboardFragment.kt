package com.example.fitpath

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    // --- MAP-RELATED VARIABLES ---
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- INITIALIZE FIREBASE ---
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // --- INITIALIZE LOCATION CLIENT ---
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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

        // 2. Community -> Goes to Community Fragment
       view.findViewById<MaterialCardView>(R.id.cardCommunity).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_communityFragment)
       }

        // 3. Start a Run -> Goes to Run Fragment
        view.findViewById<MaterialCardView>(R.id.cardStartRun).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_runFragment)
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
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                } else {
                    // Fallback to default location if last location is not available
                    val defaultLocation = LatLng(-34.0, 151.0)
                    mMap.addMarker(MarkerOptions().position(defaultLocation).title("Start Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                }
            }
        } else {
            // Fallback to default location if permission is not granted
            val defaultLocation = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(defaultLocation).title("Start Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        }
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
