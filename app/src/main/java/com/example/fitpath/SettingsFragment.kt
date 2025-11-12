package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fitpath.utils.SeedDataHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        etUsername = view.findViewById(R.id.et_settings_username)
        etEmail = view.findViewById(R.id.et_settings_email)


        val prefs = Prefs(requireContext())
        val sw = view.findViewById<Switch>(R.id.switchTheme)
        sw.isChecked = prefs.darkMode
        sw.setOnCheckedChangeListener { _, isChecked ->
            prefs.darkMode = isChecked
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }


        view.findViewById<Button>(R.id.btnSeedData)?.setOnClickListener {
            it.isEnabled = false
            Toast.makeText(requireContext(), "Seeding... please wait", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                try {
                    val seeder = SeedDataHelper()
                    val exerciseResult = seeder.seedExercises()

                    if (exerciseResult.isSuccess) {

                        val workoutResult = seeder.seedWorkouts()
                        if (workoutResult.isSuccess) {
                            Toast.makeText(requireContext(), "✓ Successfully added sample exercises AND workouts!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Error seeding workouts: ${workoutResult.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error seeding exercises: ${exerciseResult.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                    it.isEnabled = true
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    it.isEnabled = true
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        loadUserProfile()
    }


    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {

            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {

                        val username = document.getString("username")
                        val email = document.getString("email")


                        etUsername.setText(username)
                        etEmail.setText(email)
                    } else {
                        Log.w("SettingsFragment", "No user document found")
                        etUsername.setText("Error: User not found")
                        etEmail.setText("")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SettingsFragment", "Error fetching user data: ", exception)
                    etUsername.setText("Error loading data")
                    etEmail.setText("")
                }
        } else {

            etUsername.setText("Not Logged In")
            etEmail.setText("Not Logged In")
        }
    }
}
