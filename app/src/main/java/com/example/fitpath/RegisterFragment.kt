package com.example.fitpath

import android.os.Bundle
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
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = "Enter a valid email"
                return@setOnClickListener
            }


            // Make sure password has at least 8 charecters a lower and upper case letter, a symbol and a number in it.
            val passwordPattern =
                Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")
            if (!password.matches(passwordPattern)) {
                binding.passwordEditText.error =
                    "Password must be at least 8 characters, include upper & lower case, a number, and a symbol"
                return@setOnClickListener
            }

            // Make sure when user confirms password it is same as original
            if (password != confirmPassword) {
                binding.confirmPasswordEditText.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Create account with Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User is created in Firebase Auth, now save additional info in Firestore
                        val userId = auth.currentUser?.uid
                        val user = hashMapOf(
                            "email" to email

                        )

                        if (userId != null) {
                            db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Account created successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigate(R.id.action_Register_to_Login)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to save user data: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


        }
                binding.loginButton.setOnClickListener {
                    findNavController().navigate(R.id.action_Register_to_Login)
                }

        return binding.root
    }
}
