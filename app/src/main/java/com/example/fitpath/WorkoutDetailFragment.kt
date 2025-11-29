package com.example.fitpath

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitpath.adapter.WorkoutExerciseAdapter
import com.example.fitpath.repository.WorkoutRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class WorkoutDetailFragment : Fragment() {

    private lateinit var repository: WorkoutRepository
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvWorkoutName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvCreator: TextView
    private lateinit var recyclerViewExercises: RecyclerView
    private lateinit var btnStartWorkout: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var progressBar: ProgressBar

    private val auth = FirebaseAuth.getInstance()
    private var workoutId: String = ""
    private var currentWorkout: com.example.fitpath.model.Workout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = WorkoutRepository()

        workoutId = arguments?.getString("workoutId") ?: ""

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar)
        tvWorkoutName = view.findViewById(R.id.tvWorkoutName)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvCategory = view.findViewById(R.id.tvCategory)
        tvDifficulty = view.findViewById(R.id.tvDifficulty)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvCreator = view.findViewById(R.id.tvCreator)
        recyclerViewExercises = view.findViewById(R.id.recyclerViewExercises)
        btnStartWorkout = view.findViewById(R.id.btnStartWorkout)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnDelete = view.findViewById(R.id.btnDelete)
        progressBar = view.findViewById(R.id.progressBar)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        loadWorkoutDetails()

        btnStartWorkout.setOnClickListener {
            Toast.makeText(requireContext(), "Starting workout...", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to workout execution screen
        }

        btnEdit.setOnClickListener {
            editWorkout()
        }

        btnDelete.setOnClickListener {
            confirmDeleteWorkout()
        }
    }

    private fun loadWorkoutDetails() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val workout = repository.getWorkoutById(workoutId)

                if (workout != null) {
                    currentWorkout = workout

                    // Display workout info
                    tvWorkoutName.text = workout.name
                    tvDescription.text = workout.description
                    tvCategory.text = workout.category
                    tvDifficulty.text = workout.difficulty
                    tvDuration.text = "${workout.durationMinutes} min"
                    tvCreator.text = "Created by ${workout.createdByName.ifEmpty { "Anonymous" }}"

                    // Show/hide edit and delete buttons if user owns this workout
                    val isOwnWorkout = workout.createdBy == auth.currentUser?.uid
                    btnEdit.visibility = if (isOwnWorkout) View.VISIBLE else View.GONE
                    btnDelete.visibility = if (isOwnWorkout) View.VISIBLE else View.GONE

                    // Setup exercises RecyclerView - READ-ONLY (no edit/delete buttons)
                    val exerciseAdapter = WorkoutExerciseAdapter(
                        exercises = workout.exercises.toMutableList(),
                        onEditClick = { position ->
                            // No-op - exercises are read-only in detail view
                        },
                        onDeleteClick = { position ->
                            // No-op - exercises are read-only in detail view
                        },
                        showEditDelete = false  // HIDE edit/delete buttons in detail view
                    )

                    recyclerViewExercises.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = exerciseAdapter
                    }
                } else {
                    Toast.makeText(requireContext(), "Workout not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading workout: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun editWorkout() {
        // TODO: Navigate to edit screen with workout data
        Toast.makeText(requireContext(), "Edit workout: ${currentWorkout?.name}\n(Full edit mode coming soon)", Toast.LENGTH_SHORT).show()
        // You can navigate to WorkoutBuilderFragment in edit mode
        // val bundle = bundleOf("workoutId" to workoutId, "editMode" to true)
        // findNavController().navigate(R.id.action_to_workoutBuilder, bundle)
    }

    private fun confirmDeleteWorkout() {
        val workout = currentWorkout ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete \"${workout.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteWorkout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWorkout() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = repository.deleteWorkout(workoutId)
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Workout deleted", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete workout", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}