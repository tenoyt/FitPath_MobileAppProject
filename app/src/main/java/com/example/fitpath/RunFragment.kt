package com.example.fitpath

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.util.concurrent.TimeUnit


class RunFragment : Fragment(R.layout.fragment_run) {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var tvTimer: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvPace: TextView
    private lateinit var btnToggleRun: Button
    private lateinit var btnFinishRun: Button

    private var isRunning = false
    private var startTime = 0L
    private var distance = 0f
    private var lastLocation: LatLng? = null
    private val pathPoints = mutableListOf<LatLng>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { onMapReady(it) }

        tvTimer = view.findViewById(R.id.tvTimer)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvPace = view.findViewById(R.id.tvPace)
        btnToggleRun = view.findViewById(R.id.btnToggleRun)
        btnFinishRun = view.findViewById(R.id.btnFinishRun)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        btnToggleRun.setOnClickListener {
            if (isRunning) {
                pauseRun()
            } else {
                startRun()
            }
        }

        btnFinishRun.setOnClickListener { finishRun() }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    if (isRunning) {
                        if (lastLocation != null) {
                            distance += calculateDistance(lastLocation!!, currentLatLng)
                        }
                        pathPoints.add(currentLatLng)
                        lastLocation = currentLatLng
                        updateUI()
                        redrawPolyline()
                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }

    private fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            startLocationUpdates()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun startRun() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Location permission is required to start a run.", Toast.LENGTH_SHORT).show()
            return
        }
        isRunning = true
        startTime = System.currentTimeMillis()
        btnToggleRun.text = "Pause"
        btnFinishRun.visibility = View.VISIBLE
        startLocationUpdates()
        updateTimer()
    }

    private fun pauseRun() {
        isRunning = false
        btnToggleRun.text = "Resume"
    }

    private fun finishRun() {
        isRunning = false
        btnToggleRun.text = "Start Run"
        btnFinishRun.visibility = View.GONE
        // TODO: Save run data
    }

    private fun updateTimer() {
        if (isRunning) {
            val millis = System.currentTimeMillis() - startTime
            tvTimer.text = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1))
            view?.postDelayed({ updateTimer() }, 1000)
        }
    }

    private fun updateUI() {
        tvDistance.text = String.format("Distance: %.2fkm", distance / 1000)
        val pace = if (distance > 0) (System.currentTimeMillis() - startTime) / distance else 0
        // FIX: Corrected the typo from MILLISECEEDINGS to MILLISECONDS
        tvPace.text = String.format("Pace: %d'%02d''",
            TimeUnit.MILLISECONDS.toMinutes(pace.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(pace.toLong()) % 60)
    }

    private fun redrawPolyline() {
        googleMap.clear()
        googleMap.addPolyline(PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(10f))
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(2000)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
        return results[0]
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
