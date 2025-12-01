package com.example.fitpath

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitpath.adapter.WorkoutAdapter
import com.example.fitpath.repository.WorkoutRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class WorkoutLibraryFragment : Fragment() {

    private lateinit var repository: WorkoutRepository
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tabLayout: TabLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var fabCreate: FloatingActionButton

    companion object {
        private const val TAG = "WorkoutLibrary"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_workout_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        repository = WorkoutRepository()

        try {
            recyclerView = view.findViewById(R.id.recyclerViewWorkouts)
            progressBar = view.findViewById(R.id.progressBar)
            tabLayout = view.findViewById(R.id.tabLayout)
            chipGroup = view.findViewById(R.id.chipGroupCategories)
            fabCreate = view.findViewById(R.id.fabCreateWorkout)
            Log.d(TAG, "All views found successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error finding views: ${e.message}", e)
            Toast.makeText(requireContext(), "Error loading workout library", Toast.LENGTH_LONG).show()
            return
        }

        setupRecyclerView()
        setupTabs()
        setupCategoryChips()
        setupFab()

        Log.d(TAG, "Calling loadWorkouts() for initial load")
        loadWorkouts()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        workoutAdapter = WorkoutAdapter(
            onWorkoutClick = { workout ->
                Log.d(TAG, "Workout clicked: ${workout.name}, ID: ${workout.id}")

                // Navigate to workout detail screen
                val bundle = bundleOf("workoutId" to workout.id)
                findNavController().navigate(
                    R.id.action_workoutLibraryFragment_to_workoutDetailFragment,
                    bundle
                )
            },
            onFavoriteClick = { workout ->
                Log.d(TAG, "Favorite clicked: ${workout.name}")
                toggleFavorite(workout)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
        Log.d(TAG, "RecyclerView setup complete")
    }

    private fun setupTabs() {
        Log.d(TAG, "Setting up tabs")
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d(TAG, "Tab selected: position=${tab?.position}")
                when (tab?.position) {
                    0 -> {
                        Log.d(TAG, "Loading ALL workouts")
                        loadWorkouts()
                    }
                    1 -> {
                        Log.d(TAG, "Loading USER workouts")
                        loadUserWorkouts()
                    }
                    2 -> {
                        Log.d(TAG, "Loading FAVORITE workouts")
                        loadFavoriteWorkouts()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.d(TAG, "Tab unselected: position=${tab?.position}")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d(TAG, "Tab reselected: position=${tab?.position}")
            }
        })
    }

    private fun setupCategoryChips() {
        Log.d(TAG, "Setting up category chips")
        val categories = listOf("All", "Full Body", "Upper Body", "Lower Body", "Cardio", "Strength", "Flexibility")

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                if (category == "All") isChecked = true

                setOnClickListener {
                    Log.d(TAG, "Category chip clicked: $category")
                    (chipGroup.getChildAt(0) as Chip).isChecked = category == "All"
                    if (category == "All") {
                        loadWorkouts()
                    } else {
                        loadWorkoutsByCategory(category)
                    }
                }
            }
            chipGroup.addView(chip)
        }
        Log.d(TAG, "Category chips setup complete")
    }

    private fun setupFab() {
        Log.d(TAG, "Setting up FAB")
        fabCreate.setOnClickListener {
            Log.d(TAG, "FAB clicked - navigating to workout builder")
            findNavController().navigate(R.id.action_workoutLibraryFragment_to_workoutBuilderFragment)
        }
    }

    private fun loadWorkouts() {
        Log.d(TAG, "=== loadWorkouts() START ===")
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling repository.getAllPublicWorkouts()")
                val workouts = repository.getAllPublicWorkouts()
                Log.d(TAG, "Repository returned ${workouts.size} workouts")

                workouts.forEachIndexed { index, workout ->
                    Log.d(TAG, "Workout $index: ${workout.name}, ID: ${workout.id}, isPublic=${workout.isPublic}")
                }

                Log.d(TAG, "Submitting workouts to adapter")
                workoutAdapter.submitList(workouts)

                if (workouts.isEmpty()) {
                    Log.w(TAG, "No workouts found")
                    Toast.makeText(requireContext(), "No workouts found. Create one to get started!", Toast.LENGTH_LONG).show()
                } else {
                    Log.d(TAG, "Successfully loaded ${workouts.size} workouts")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading workouts: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to load workouts: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                Log.d(TAG, "=== loadWorkouts() END ===")
            }
        }
    }

    private fun loadUserWorkouts() {
        Log.d(TAG, "=== loadUserWorkouts() START ===")
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling repository.getUserWorkouts()")
                val workouts = repository.getUserWorkouts()
                Log.d(TAG, "Repository returned ${workouts.size} user workouts")

                workoutAdapter.submitList(workouts)

                if (workouts.isEmpty()) {
                    Log.w(TAG, "No user workouts found")
                    Toast.makeText(requireContext(), "You haven't created any workouts yet", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Successfully loaded ${workouts.size} user workouts")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user workouts: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to load workouts: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                Log.d(TAG, "=== loadUserWorkouts() END ===")
            }
        }
    }

    private fun loadFavoriteWorkouts() {
        Log.d(TAG, "=== loadFavoriteWorkouts() START ===")
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling repository.getUserFavoriteWorkouts()")
                val workouts = repository.getUserFavoriteWorkouts()
                Log.d(TAG, "Repository returned ${workouts.size} favorite workouts")

                workoutAdapter.submitList(workouts)

                if (workouts.isEmpty()) {
                    Log.w(TAG, "No favorite workouts found")
                    Toast.makeText(requireContext(), "You haven't favorited any workouts yet", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Successfully loaded ${workouts.size} favorite workouts")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading favorites: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to load favorites: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                Log.d(TAG, "=== loadFavoriteWorkouts() END ===")
            }
        }
    }

    private fun loadWorkoutsByCategory(category: String) {
        Log.d(TAG, "=== loadWorkoutsByCategory($category) START ===")
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling repository.getWorkoutsByCategory($category)")
                val workouts = repository.getWorkoutsByCategory(category)
                Log.d(TAG, "Repository returned ${workouts.size} workouts for category $category")

                workoutAdapter.submitList(workouts)

                if (workouts.isEmpty()) {
                    Log.w(TAG, "No $category workouts found")
                    Toast.makeText(requireContext(), "No $category workouts found", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Successfully loaded ${workouts.size} $category workouts")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading workouts by category: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to load workouts: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                Log.d(TAG, "=== loadWorkoutsByCategory($category) END ===")
            }
        }
    }

    private fun toggleFavorite(workout: com.example.fitpath.model.Workout) {
        Log.d(TAG, "=== toggleFavorite(${workout.name}) START ===")

        lifecycleScope.launch {
            try {
                if (workout.isFavorite) {
                    Log.d(TAG, "Removing ${workout.name} from favorites")
                    repository.removeFromFavorites(workout.id)
                    Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Adding ${workout.name} to favorites")
                    repository.addToFavorites(workout)
                    Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
                }

                // Reload current view
                Log.d(TAG, "Reloading view after favorite toggle")
                when (tabLayout.selectedTabPosition) {
                    0 -> loadWorkouts()
                    1 -> loadUserWorkouts()
                    2 -> loadFavoriteWorkouts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to update favorites: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}