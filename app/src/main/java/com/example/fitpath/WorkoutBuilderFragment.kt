package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitpath.adapter.WorkoutExerciseAdapter
import com.example.fitpath.databinding.FragmentWorkoutBuilderBinding
import com.example.fitpath.model.Workout
import com.example.fitpath.model.WorkoutExercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutBuilderFragment : Fragment() {

    private var _binding: FragmentWorkoutBuilderBinding? = null
    private val binding get() = _binding!!

    // The list of exercises for the current workout being built. This is the source of truth.
    private val exercisesList = mutableListOf<WorkoutExercise>()

    // The adapter for the RecyclerView.
    private lateinit var workoutExerciseAdapter: WorkoutExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBuilderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Define the functions that will handle clicks inside the adapter.
        val onEditClick = { position: Int ->
            val exercise = exercisesList[position]
            Toast.makeText(context, "TODO: Edit ${exercise.exerciseName}", Toast.LENGTH_SHORT).show()
        }

        val onDeleteClick = { position: Int ->
            val exerciseName = exercisesList[position].exerciseName
            exercisesList.removeAt(position) // Remove from the source of truth
            workoutExerciseAdapter.notifyItemRemoved(position) // Tell the adapter to animate the removal
            workoutExerciseAdapter.notifyItemRangeChanged(position, exercisesList.size) // Update subsequent item positions
            Toast.makeText(context, "$exerciseName removed", Toast.LENGTH_SHORT).show()
        }

        // 2. Initialize the adapter, passing it the list and the click handlers it requires.
        workoutExerciseAdapter = WorkoutExerciseAdapter(exercisesList, onEditClick, onDeleteClick)

        // 3. Setup the RecyclerView with the adapter.
        binding.recyclerViewExercises.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workoutExerciseAdapter
        }

        // Setup the UI elements
        setupSpinners()

        // 4. Set up the button to add a new exercise.
        binding.btnAddExercise.setOnClickListener {
            Log.d("WorkoutBuilder", "Add Exercise button clicked.")

            // Create a placeholder exercise.
            val dummyWorkoutExercise = WorkoutExercise(
                exerciseName = "New Exercise",
                sets = 3,
                reps = 10,
                restTime = 60,
                notes = "Tap to edit details."
            )

            // Add the new exercise to our list and notify the adapter.
            exercisesList.add(dummyWorkoutExercise)
            workoutExerciseAdapter.notifyItemInserted(exercisesList.size - 1)
        }

        binding.btnSaveWorkout.setOnClickListener {
            saveWorkout()
        }
    }

    private fun setupSpinners() {
        // Setup Category Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.workout_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }

        // Setup Difficulty Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.workout_difficulties,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDifficulty.adapter = adapter
        }
    }

    private fun saveWorkout() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val authorId = firebaseAuth.currentUser?.uid

        if (authorId == null) {
            Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_LONG).show()
            return
        }

        val workoutName = binding.editTextWorkoutName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val difficulty = binding.spinnerDifficulty.selectedItem.toString()
        val isPublic = binding.switchPublic.isChecked

        // 5. Use the fragment's 'exercisesList' as the data to save.
        val exercisesToSave = exercisesList

        if (workoutName.isEmpty()) {
            Toast.makeText(context, "Workout name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        if (exercisesToSave.isEmpty()) {
            Toast.makeText(context, "A workout must have at least one exercise.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val newWorkout = Workout(
            name = workoutName,
            description = description,
            category = category,
            difficulty = difficulty,
            isPublic = isPublic,
            createdBy = authorId, // Use the correct field name 'createdBy'
            exercises = exercisesToSave
        )

        firestore.collection("workouts").add(newWorkout)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Workout saved successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to save workout: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("WorkoutBuilder", "Error saving workout", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
