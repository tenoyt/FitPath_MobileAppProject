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

    // Authentication Variables
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Elements
    private lateinit var loginBtn: Button
    private lateinit var welcomeText: TextView
    private lateinit var signOutBtn: Button
    private lateinit var profileBtn: ImageButton

    // Map Variables
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Find all UI views
        loginBtn = view.findViewById(R.id.btnLoginRegister)
        welcomeText = view.findViewById(R.id.tvWelcome)
        signOutBtn = view.findViewById(R.id.btnSignOut)
        profileBtn = view.findViewById(R.id.btn_profile)

        // Map Initialization
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        view.findViewById<View>(R.id.cardMapView)?.visibility = View.VISIBLE

        view.findViewById<MaterialCardView>(R.id.cardLogWorkout).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_workoutLibraryFragment)
        }

        view.findViewById<MaterialCardView>(R.id.cardLogMeal).setOnClickListener {
            Toast.makeText(requireContext(), "Meal logging coming soon!", Toast.LENGTH_SHORT).show()
        }

       view.findViewById<MaterialCardView>(R.id.cardCommunity).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_communityFragment)
       }

        view.findViewById<MaterialCardView>(R.id.cardStartRun).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_runFragment)
        }

        // Authentication Listeners
        loginBtn.setOnClickListener {
            findNavController().navigate(R.id.Login)
        }

        signOutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        profileBtn.setOnClickListener {
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.settings
        }
    }

    // Map Implementation
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

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun updateUI() {
        val currentUser = auth.currentUser
        welcomeText.visibility = View.VISIBLE

        if (currentUser != null) {
            // User Is Logged In
            loginBtn.visibility = View.GONE
            signOutBtn.visibility = View.VISIBLE
            profileBtn.visibility = View.VISIBLE

            fetchUsername(currentUser.uid)
        } else {
            // User Isn't Logged In
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
