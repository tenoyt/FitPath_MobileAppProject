package com.example.fitpath.repository

import com.example.fitpath.model.Exercise
import com.example.fitpath.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val exercisesCollection = db.collection("exercises")
    private val workoutsCollection = db.collection("workouts")
    private val userWorkoutsCollection = db.collection("user_workouts")

    // ===== EXERCISE OPERATIONS =====

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

    // ===== WORKOUT OPERATIONS =====

    suspend fun getAllPublicWorkouts(): List<Workout> {
        return try {
            // FIXED: Query for "public" field (not "isPublic") to match Firebase structure
            workoutsCollection
                .whereEqualTo("public", true)  // Changed from "isPublic" to "public"
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWorkoutsByCategory(category: String): List<Workout> {
        return try {
            workoutsCollection
                .whereEqualTo("public", true)  // Changed from "isPublic" to "public"
                .whereEqualTo("category", category)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWorkoutById(workoutId: String): Workout? {
        return try {
            workoutsCollection
                .document(workoutId)
                .get()
                .await()
                .toObject(Workout::class.java)?.copy(id = workoutId)
        } catch (e: Exception) {
            null
        }
    }

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

    suspend fun getUserWorkouts(): List<Workout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            workoutsCollection
                .whereEqualTo("createdBy", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserFavoriteWorkouts(): List<Workout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            userWorkoutsCollection
                .document(userId)
                .collection("favorites")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Workout::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

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