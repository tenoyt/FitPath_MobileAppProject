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
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.loginButton.setOnClickListener {
            loginUser()
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_Login_to_Register)
        }

        return binding.root
    }

    private fun loginUser() {
        val identifier = binding.emailEditText.text.toString().trim() // This can be email OR username
        val password = binding.passwordEditText.text.toString().trim()

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            Log.d(TAG, "Identifier is an email. Signing in directly.")
            signInWithFirebase(identifier, password)
        } else {
            Log.d(TAG, "Identifier is a username. Querying Firestore...")
            findEmailForUsername(identifier, password)
        }
    }

    private fun findEmailForUsername(username: String, password: String) {
        Log.d(TAG, "Attempting to find email for username: $username")

        db.collection("users").whereEqualTo("username", username).limit(1).get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Query successful, found ${documents.size()} documents.")

                if (documents.isEmpty) {
                    Log.w(TAG, "Username '$username' not found in database (is it case-sensitive?).")
                    showLoading(false)
                    Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
                } else {
                    val email = documents.first().getString("email")
                    if (email != null) {
                        Log.d(TAG, "Username found! Email is: $email. Attempting login...")
                        signInWithFirebase(email, password)
                    } else {
                        Log.e(TAG, "User document found, but 'email' field is null or missing.")
                        showLoading(false)
                        Toast.makeText(requireContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error querying for username: ${e.message}", e) // This logs the real error
                showLoading(false)
                Toast.makeText(requireContext(), "Error checking user data. Check Logcat.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()


                   findNavController().navigate(R.id.action_Login_to_dashboard)
                } else {
                    Log.w(TAG, "Firebase signInWithEmailAndPassword failed: ${task.exception?.message}")
                    showLoading(false)
                    Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginButton.isEnabled = false
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.loginButton.isEnabled = true
        }
    }
}