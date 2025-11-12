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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class DashboardFragment : Fragment(R.layout.fragment_dashboard), OnMapReadyCallback{

    private lateinit var loginBtn: Button

    private lateinit var sessionManager: SessionManager

    private lateinit var mMap: GoogleMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        loginBtn = view.findViewById(R.id.btnLoginRegister)

        // Workout and Meal cards
        view.findViewById<MaterialCardView>(R.id.cardLogWorkout).setOnClickListener {
            Toast.makeText(requireContext(), "Workout logging coming soon!", Toast.LENGTH_SHORT)
                .show()
        }
        view.findViewById<MaterialCardView>(R.id.cardLogMeal).setOnClickListener {
            Toast.makeText(requireContext(), "Meal logging coming soon!", Toast.LENGTH_SHORT).show()
        }



        updateLoginButton()

        // Initialize and get notified when the map is ready
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Make map card visible
        view.findViewById<View>(R.id.cardMapView).visibility = View.VISIBLE
    }

    // This is called when the map is ready to use
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker (Sydney in example)
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12f))

        // Optional: enable zoom controls
        mMap.uiSettings.isZoomControlsEnabled = true
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
