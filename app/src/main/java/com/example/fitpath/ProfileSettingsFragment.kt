package com.example.fitpath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.widget.ImageButton

class ProfileSettingsFragment : Fragment() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etCurrentPass: EditText
    private lateinit var etNewPass: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var btnBack: ImageButton

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername = view.findViewById(R.id.etEditUsername)
        etEmail = view.findViewById(R.id.etEditEmail)
        etCurrentPass = view.findViewById(R.id.etCurrentPassword)
        etNewPass = view.findViewById(R.id.etNewPassword)
        btnSave = view.findViewById(R.id.btnSaveChanges)
        progressBar = view.findViewById(R.id.progressBarProfile)
        btnBack = view.findViewById(R.id.btnBackToSettings)
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        loadUserData()

        btnSave.setOnClickListener {
            val currentPass = etCurrentPass.text.toString()
            if (currentPass.isEmpty()) {
                etCurrentPass.error = "Current password is required to make changes"
                return@setOnClickListener
            }
            updateProfile(currentPass)
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            etEmail.setText(user.email)

            // Fetch username from Firestore "users" collection
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        etUsername.setText(document.getString("username"))
                    }
                }
        }
    }

    private fun updateProfile(currentPass: String) {
        val user = auth.currentUser ?: return
        val newUsername = etUsername.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        val newPass = etNewPass.text.toString().trim()

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        // 1. Re-authenticate user
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                // Re-auth success, proceed with updates

                // Update Email if changed
                if (newEmail != user.email) {
                    user.updateEmail(newEmail)
                }

                // Update Password if field is not empty
                if (newPass.isNotEmpty()) {
                    if (newPass.length < 6) {
                        Toast.makeText(context, "New password too short", Toast.LENGTH_SHORT).show()
                    } else {
                        user.updatePassword(newPass)
                    }
                }


                // Update Username in Firestore
                val userMap = hashMapOf("username" to newUsername)

                db.collection("users").document(user.uid)
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        btnSave.isEnabled = true
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp() // Go back to Settings
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        btnSave.isEnabled = true
                        Toast.makeText(context, "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } else {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(context, "Incorrect current password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
