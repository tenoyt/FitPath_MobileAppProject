package com.example.fitpath

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitpath.adapter.WorkoutExerciseAdapter
import com.example.fitpath.databinding.FragmentWorkoutBuilderBinding
import com.example.fitpath.model.Workout
import com.example.fitpath.model.WorkoutExercise
import com.example.fitpath.repository.WorkoutRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class WorkoutBuilderFragment : Fragment() {

    private var _binding: FragmentWorkoutBuilderBinding? = null
    private val binding get() = _binding!!

    private val exercisesList = mutableListOf<WorkoutExercise>()
    private lateinit var workoutExerciseAdapter: WorkoutExerciseAdapter

    // Edit mode variables
    private var isEditMode = false
    private var editWorkoutId: String? = null
    private val repository = WorkoutRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBuilderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if we're in edit mode
        editWorkoutId = arguments?.getString("workoutId")
        isEditMode = editWorkoutId != null

        Log.d("WorkoutBuilder", "Edit mode: $isEditMode, workoutId: $editWorkoutId")

        // Define click handlers for adapter
        val onEditClick = { position: Int ->
            showEditExerciseDialog(position)
        }

        val onDeleteClick = { position: Int ->
            val exerciseName = exercisesList[position].exerciseName
            exercisesList.removeAt(position)
            workoutExerciseAdapter.notifyItemRemoved(position)
            workoutExerciseAdapter.notifyItemRangeChanged(position, exercisesList.size)
            Toast.makeText(context, "$exerciseName removed", Toast.LENGTH_SHORT).show()
        }

        // Initialize adapter
        workoutExerciseAdapter = WorkoutExerciseAdapter(exercisesList, onEditClick, onDeleteClick)

        // Setup RecyclerView
        binding.recyclerViewExercises.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workoutExerciseAdapter
        }

        // Setup spinners
        setupSpinners()

        // Load workout if in edit mode
        if (isEditMode) {
            loadWorkoutForEdit()
            binding.btnSaveWorkout.text = "UPDATE WORKOUT"
        }

        // Add exercise button
        binding.btnAddExercise.setOnClickListener {
            val dummyWorkoutExercise = WorkoutExercise(
                exerciseName = "New Exercise",
                sets = 3,
                reps = 10,
                restTime = 60,
                notes = "Tap to edit details."
            )
            exercisesList.add(dummyWorkoutExercise)
            workoutExerciseAdapter.notifyItemInserted(exercisesList.size - 1)
        }

        // Save/Update button
        binding.btnSaveWorkout.setOnClickListener {
            if (isEditMode) {
                updateWorkout()
            } else {
                saveWorkout()
            }
        }
    }

    private fun loadWorkoutForEdit() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val workout = repository.getWorkoutById(editWorkoutId ?: "")

                if (workout != null) {
                    // Pre-fill form fields
                    binding.editTextWorkoutName.setText(workout.name)
                    binding.editTextDescription.setText(workout.description)
                    binding.switchPublic.isChecked = workout.isPublic

                    // Set category spinner
                    val categories = resources.getStringArray(R.array.workout_categories)
                    val categoryPosition = categories.indexOf(workout.category)
                    if (categoryPosition >= 0) {
                        binding.spinnerCategory.setSelection(categoryPosition)
                    }

                    // Set difficulty spinner
                    val difficulties = resources.getStringArray(R.array.workout_difficulties)
                    val difficultyPosition = difficulties.indexOf(workout.difficulty)
                    if (difficultyPosition >= 0) {
                        binding.spinnerDifficulty.setSelection(difficultyPosition)
                    }

                    // Load exercises
                    exercisesList.clear()
                    exercisesList.addAll(workout.exercises)
                    workoutExerciseAdapter.notifyDataSetChanged()

                    Toast.makeText(context, "Loaded workout for editing", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to load workout", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                Log.e("WorkoutBuilder", "Error loading workout", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
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

    private fun showEditExerciseDialog(position: Int) {
        val exercise = exercisesList[position]

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_exercise, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etExerciseName)
        val etSets = dialogView.findViewById<TextInputEditText>(R.id.etSets)
        val etReps = dialogView.findViewById<TextInputEditText>(R.id.etReps)
        val etDuration = dialogView.findViewById<TextInputEditText>(R.id.etDuration)
        val etRest = dialogView.findViewById<TextInputEditText>(R.id.etRest)
        val etNotes = dialogView.findViewById<TextInputEditText>(R.id.etNotes)

        etName.setText(exercise.exerciseName)
        etSets.setText(exercise.sets.toString())
        etReps.setText(exercise.reps.toString())
        etDuration.setText(exercise.duration.toString())
        etRest.setText(exercise.restTime.toString())
        etNotes.setText(exercise.notes)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Exercise")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val name = etName.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(context, "Exercise name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val sets = etSets.text.toString().toIntOrNull() ?: 0
                val reps = etReps.text.toString().toIntOrNull() ?: 0
                val duration = etDuration.text.toString().toIntOrNull() ?: 0
                val rest = etRest.text.toString().toIntOrNull() ?: 0
                val notes = etNotes.text.toString().trim()

                exercisesList[position] = WorkoutExercise(
                    exerciseId = exercise.exerciseId,
                    exerciseName = name,
                    sets = sets,
                    reps = reps,
                    duration = duration,
                    restTime = rest,
                    notes = notes
                )

                workoutExerciseAdapter.notifyItemChanged(position)
                Toast.makeText(context, "Exercise updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateWorkout() {
        val firebaseAuth = FirebaseAuth.getInstance()
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

        if (workoutName.isEmpty()) {
            Toast.makeText(context, "Workout name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        if (exercisesList.isEmpty()) {
            Toast.makeText(context, "A workout must have at least one exercise.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val updatedWorkout = Workout(
                    id = editWorkoutId ?: "",
                    name = workoutName,
                    description = description,
                    category = category,
                    difficulty = difficulty,
                    isPublic = isPublic,
                    createdBy = authorId,
                    exercises = exercisesList
                )

                val result = repository.updateWorkout(editWorkoutId ?: "", updatedWorkout)

                if (result.isSuccess) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Workout updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to update workout", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("WorkoutBuilder", "Error updating workout", e)
            }
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

        if (workoutName.isEmpty()) {
            Toast.makeText(context, "Workout name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        if (exercisesList.isEmpty()) {
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
            createdBy = authorId,
            exercises = exercisesList
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