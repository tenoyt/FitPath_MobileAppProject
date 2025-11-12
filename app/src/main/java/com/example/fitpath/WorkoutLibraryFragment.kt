package com.example.fitpath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // <-- 1. IMPORT THIS
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = WorkoutRepository()

        recyclerView = view.findViewById(R.id.recyclerViewWorkouts)
        progressBar = view.findViewById(R.id.progressBar)
        tabLayout = view.findViewById(R.id.tabLayout)
        chipGroup = view.findViewById(R.id.chipGroupCategories)
        fabCreate = view.findViewById(R.id.fabCreateWorkout)

        setupRecyclerView()
        setupTabs()
        setupCategoryChips()
        setupFab()

        loadWorkouts()
    }

    // ... (keep setupRecyclerView, setupTabs, setupCategoryChips)

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutAdapter(
            onWorkoutClick = { workout ->
                Toast.makeText(requireContext(), "View workout: ${workout.name}", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { workout ->
                toggleFavorite(workout)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadWorkouts() // All Workouts
                    1 -> loadUserWorkouts() // My Workouts
                    2 -> loadFavoriteWorkouts() // Favorites
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupCategoryChips() {
        val categories = listOf("All", "Full Body", "Upper Body", "Lower Body", "Cardio", "Strength", "Flexibility")

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                if (category == "All") isChecked = true

                setOnClickListener {
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
    }


    private fun setupFab() {
        // 2. CHANGE THE CLICK LISTENER
        fabCreate.setOnClickListener {
            // This assumes you have a navigation action defined in your nav_graph.xml
            // with this ID that leads from WorkoutLibraryFragment to WorkoutBuilderFragment.
            findNavController().navigate(R.id.action_workoutLibraryFragment_to_workoutBuilderFragment)
        }
    }

    // ... (keep all the load and toggle functions)
    private fun loadWorkouts() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val workouts = repository.getAllPublicWorkouts()
                workoutAdapter.submitList(workouts)
                if (workouts.isEmpty()) {
                    Toast.makeText(requireContext(), "No workouts found. Create one to get started!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load workouts: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadUserWorkouts() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val workouts = repository.getUserWorkouts()
                workoutAdapter.submitList(workouts)
                if (workouts.isEmpty()) {
                    Toast.makeText(requireContext(), "You haven't created any workouts yet", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load workouts: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadFavoriteWorkouts() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val workouts = repository.getUserFavoriteWorkouts()
                workoutAdapter.submitList(workouts)
                if (workouts.isEmpty()) {
                    Toast.makeText(requireContext(), "You haven't favorited any workouts yet", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load favorites: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }


    private fun loadWorkoutsByCategory(category: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val workouts = repository.getWorkoutsByCategory(category)
                workoutAdapter.submitList(workouts)
                if (workouts.isEmpty()) {
                    Toast.makeText(requireContext(), "No $category workouts found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load workouts: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun toggleFavorite(workout: com.example.fitpath.model.Workout) {
        lifecycleScope.launch {
            try {
                if (workout.isFavorite) {
                    repository.removeFromFavorites(workout.id)
                    Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
                } else {
                    repository.addToFavorites(workout)
                    Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
                }
                // Reload current view
                when (tabLayout.selectedTabPosition) {
                    0 -> loadWorkouts()
                    1 -> loadUserWorkouts()
                    2 -> loadFavoriteWorkouts()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to update favorites: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
