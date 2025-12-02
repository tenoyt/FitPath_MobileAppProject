package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fitpath.databinding.RegisterFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var binding: RegisterFragmentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterFragmentBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth and Firestore instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set click listener for the registration button
        binding.registerButton.setOnClickListener {
            registerUser() // Call the function to handle user registration logic
        }

        // Set click listener for the login button to navigate to the login screen
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_Register_to_Login)
        }

        return binding.root
    }

    private fun registerUser() {
        // Retrieve input from EditText fields
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val username = binding.usernameEditText.text.toString().trim()

        // Validate that no fields are empty
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate username format
        val usernamePattern = Regex("^[a-zA-Z0-9]{4,12}$")
        if (!username.matches(usernamePattern)) {
            binding.usernameEditText.error = "Username must be 4-12 characters and only contain letters and numbers."
            return
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Enter a valid email"
            return
        }

        // Validate password strength
        val passwordPattern =
            Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")
        if (!password.matches(passwordPattern)) {
            binding.passwordEditText.error =
                "Password must be at least 8 characters, include upper & lower case, a number, and a symbol"
            return
        }

        // Check if password and confirm password match
        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        // Show loading indicator while checking database
        showLoading(true)

        // Check if the username already exists in the Firestore database
        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Username is unique, proceed with creating the user in Firebase Auth
                    createUserInFirebase(email, password, username)
                } else {
                    // Username is already taken
                    binding.usernameEditText.error = "This username is already taken"
                    showLoading(false)
                }
            }
            .addOnFailureListener { e ->
                // Handle database query failure
                Toast.makeText(requireContext(), "Error checking username: ${e.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
    }

    private fun createUserInFirebase(email: String, password: String, username: String) {
        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    // Create a user map to store in Firestore (excluding password)
                    val user = hashMapOf(
                        "email" to email,
                        "username" to username // Save the unique username
                    )

                    if (userId != null) {
                        // Save user details to Firestore under the user's UID
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                showLoading(false)
                                Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                                // Navigate to login screen upon successful registration
                                findNavController().navigate(R.id.action_Register_to_Login)
                            }
                            .addOnFailureListener { e ->
                                showLoading(false)
                                Toast.makeText(requireContext(), "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Handle registration failure (e.g., email already in use)
                    showLoading(false)
                    Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        // Toggle visibility of progress bar and enable/disable register button
        if (isLoading) {
            binding.registerProgressBar.visibility = View.VISIBLE
            binding.registerButton.isEnabled = false
        } else {
            binding.registerProgressBar.visibility = View.GONE
            binding.registerButton.isEnabled = true
        }
    }
}