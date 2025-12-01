package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fitpath.databinding.LoginFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fitpath.R


class LoginFragment : Fragment() {


    private lateinit var binding: LoginFragmentBinding
    // Firestore instance for database operations
    private lateinit var db: FirebaseFirestore
    // FirebaseAuth instance for authentication operations
    private lateinit var auth: FirebaseAuth


    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LoginFragmentBinding.inflate(inflater, container, false)
        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set click listener for the login button
        binding.loginButton.setOnClickListener {
            loginUser() // Call the login function
        }

        // Set click listener for the register button
        binding.registerButton.setOnClickListener {
            // Navigate to the registration screen
            findNavController().navigate(R.id.action_Login_to_Register)
        }

        return binding.root
    }

    private fun loginUser() {
        // Get the identifier (email or username) and password from input fields
        val identifier = binding.emailEditText.text.toString().trim() // This can be email OR username
        val password = binding.passwordEditText.text.toString().trim()

        // Check if fields are empty
        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading indicator
        showLoading(true)

        // Check if the identifier is a valid email address
        if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            Log.d(TAG, "Identifier is an email. Signing in directly.")
            // If it's an email, sign in directly with Firebase Auth
            signInWithFirebase(identifier, password)
        } else {
            Log.d(TAG, "Identifier is a username. Querying Firestore...")
            // If it's not an email (assumed to be a username), find the email associated with it first
            findEmailForUsername(identifier, password)
        }
    }

    private fun findEmailForUsername(username: String, password: String) {
        Log.d(TAG, "Attempting to find email for username: $username")

        // Query the 'users' collection in Firestore for a document where 'username' matches the input
        db.collection("users").whereEqualTo("username", username).limit(1).get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Query successful, found ${documents.size()} documents.")

                if (documents.isEmpty) {
                    // Username not found in database
                    Log.w(TAG, "Username '$username' not found in database (is it case-sensitive?).")
                    showLoading(false)
                    Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
                } else {
                    // Username found, retrieve the associated email
                    val email = documents.first().getString("email")
                    if (email != null) {
                        Log.d(TAG, "Username found! Email is: $email. Attempting login...")
                        // Sign in using the retrieved email and provided password
                        signInWithFirebase(email, password)
                    } else {
                        // Document found but email field is missing/null
                        Log.e(TAG, "User document found, but 'email' field is null or missing.")
                        showLoading(false)
                        Toast.makeText(requireContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                // Database query failed
                Log.e(TAG, "Error querying for username: ${e.message}", e) // This logs the real error
                showLoading(false)
                Toast.makeText(requireContext(), "Error checking user data. Check Logcat.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInWithFirebase(email: String, password: String) {
        // Attempt to sign in with email and password using Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful
                    showLoading(false)
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to the dashboard screen
                    findNavController().navigate(R.id.action_Login_to_dashboard)
                } else {
                    // Login failed
                    Log.w(TAG, "Firebase signInWithEmailAndPassword failed: ${task.exception?.message}")
                    showLoading(false)
                    Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        // Toggle visibility of progress bar and enable/disable login button
        if (isLoading) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginButton.isEnabled = false
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.loginButton.isEnabled = true
        }
    }
}