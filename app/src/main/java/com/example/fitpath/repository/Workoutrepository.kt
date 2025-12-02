package com.example.fitpath.repository

import com.example.fitpath.model.Exercise
import com.example.fitpath.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class WorkoutRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val exercisesCollection = db.collection("exercises")
    private val workoutsCollection = db.collection("workouts")
    private val userWorkoutsCollection = db.collection("user_workouts")

    // Exercise Operations

    suspend fun getAllPublicExercises(): List<Exercise> {
        return try {
            exercisesCollection
                .whereEqualTo("isPublic", true)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Exercise::class.java)?.copy(id = it.id) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getExercisesByCategory(category: String): List<Exercise> {
        return try {
            exercisesCollection
                .whereEqualTo("isPublic", true)
                .whereEqualTo("category", category)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Exercise::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getExerciseById(exerciseId: String): Exercise? {
        return try {
            exercisesCollection
                .document(exerciseId)
                .get()
                .await()
                .toObject(Exercise::class.java)?.copy(id = exerciseId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createExercise(exercise: Exercise): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val exerciseData = exercise.copy(createdBy = userId)
            val docRef = exercisesCollection.add(exerciseData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserExercises(): List<Exercise> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            exercisesCollection
                .whereEqualTo("createdBy", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Exercise::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Workout Operations

    suspend fun getAllPublicWorkouts(): List<Workout> {
        return try {
            val userId = auth.currentUser?.uid

            val workouts = workoutsCollection
                .whereEqualTo("public", true)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id) }
                .sortedByDescending { it.timestamp }  // Sort in memory instead

            // Check favorite status for each workout
            if (userId != null) {
                val favoriteIds = getFavoriteWorkoutIds(userId)
                return workouts.map { workout ->
                    workout.copy(isFavorite = favoriteIds.contains(workout.id))
                }
            }

            workouts
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWorkoutsByCategory(category: String): List<Workout> {
        return try {
            val userId = auth.currentUser?.uid

            val workouts = workoutsCollection
                .whereEqualTo("public", true)
                .whereEqualTo("category", category)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id) }

            // Check favorite status
            if (userId != null) {
                val favoriteIds = getFavoriteWorkoutIds(userId)
                return workouts.map { workout ->
                    workout.copy(isFavorite = favoriteIds.contains(workout.id))
                }
            }

            workouts
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWorkoutById(workoutId: String): Workout? {
        return try {
            val workout = workoutsCollection
                .document(workoutId)
                .get()
                .await()
                .toObject(Workout::class.java)?.copy(id = workoutId)

            // Check if it's favorited
            val userId = auth.currentUser?.uid
            if (workout != null && userId != null) {
                val isFavorited = checkIfFavorited(userId, workoutId)
                return workout.copy(isFavorite = isFavorited)
            }

            workout
        } catch (e: Exception) {
            null
        }
    }

    // Create workout
    suspend fun createWorkout(workout: Workout): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val workoutData = workout.copy(createdBy = userId)
            val docRef = workoutsCollection.add(workoutData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update workout
    suspend fun updateWorkout(workoutId: String, workout: Workout): Result<Unit> {
        return try {
            workoutsCollection
                .document(workoutId)
                .set(workout)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete workout
    suspend fun deleteWorkout(workoutId: String): Result<Unit> {
        return try {
            workoutsCollection
                .document(workoutId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user workouts
    suspend fun getUserWorkouts(): List<Workout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val workouts = workoutsCollection
                .whereEqualTo("createdBy", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id) }

            // Check favorite status
            val favoriteIds = getFavoriteWorkoutIds(userId)
            workouts.map { workout ->
                workout.copy(isFavorite = favoriteIds.contains(workout.id))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get favorite workouts
    suspend fun getUserFavoriteWorkouts(): List<Workout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            userWorkoutsCollection
                .document(userId)
                .collection("favorites")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id, isFavorite = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Helper function to get list of favorited workout IDs
    private suspend fun getFavoriteWorkoutIds(userId: String): Set<String> {
        return try {
            userWorkoutsCollection
                .document(userId)
                .collection("favorites")
                .get()
                .await()
                .documents
                .map { it.id }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // Helper function to check if a specific workout is favorited
    private suspend fun checkIfFavorited(userId: String, workoutId: String): Boolean {
        return try {
            val doc = userWorkoutsCollection
                .document(userId)
                .collection("favorites")
                .document(workoutId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // Add to favorites
    suspend fun addToFavorites(workout: Workout): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            userWorkoutsCollection
                .document(userId)
                .collection("favorites")
                .document(workout.id)
                .set(workout.copy(isFavorite = true))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove from favorites
    suspend fun removeFromFavorites(workoutId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            userWorkoutsCollection
                .document(userId)
                .collection("favorites")
                .document(workoutId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}