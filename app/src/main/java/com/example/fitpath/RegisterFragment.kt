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
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.registerButton.setOnClickListener {
            registerUser() // We moved the logic to a new function
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_Register_to_Login)
        }

        return binding.root
    }

    private fun registerUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val username = binding.usernameEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val usernamePattern = Regex("^[a-zA-Z0-9]{4,12}$")
        if (!username.matches(usernamePattern)) {
            binding.usernameEditText.error = "Username must be 4-12 characters and only contain letters and numbers."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Enter a valid email"
            return
        }

        val passwordPattern =
            Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")
        if (!password.matches(passwordPattern)) {
            binding.passwordEditText.error =
                "Password must be at least 8 characters, include upper & lower case, a number, and a symbol"
            return
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        showLoading(true)

        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    createUserInFirebase(email, password, username)
                } else {
                    binding.usernameEditText.error = "This username is already taken"
                    showLoading(false)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error checking username: ${e.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
    }

    private fun createUserInFirebase(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "email" to email,
                        "username" to username // Save the unique username
                    )

                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                showLoading(false)
                                Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_Register_to_Login)
                            }
                            .addOnFailureListener { e ->
                                showLoading(false)
                                Toast.makeText(requireContext(), "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Auth creation failed (e.g., email already in use)
                    showLoading(false)
                    Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.registerProgressBar.visibility = View.VISIBLE
            binding.registerButton.isEnabled = false
        } else {
            binding.registerProgressBar.visibility = View.GONE
            binding.registerButton.isEnabled = true
        }
    }
}