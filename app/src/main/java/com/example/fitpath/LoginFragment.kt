package com.example.fitpath

import android.os.Bundle
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

class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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

            signInWithFirebase(identifier, password)
        } else {
            findEmailForUsername(identifier, password)
        }
    }

    private fun findEmailForUsername(username: String, password: String) {
        db.collection("users").whereEqualTo("username", username).limit(1).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {

                    showLoading(false)
                    Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
                } else {

                    val email = documents.first().getString("email")
                    if (email != null) {
                        signInWithFirebase(email, password)
                    } else {
                        // Data error
                        showLoading(false)
                        Toast.makeText(requireContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Error checking user data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_Login_to_Dashboard)
                } else {
                    showLoading(false)
                    // Use a generic error for security
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