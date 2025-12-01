package com.example.fitpath.utils

import com.example.fitpath.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.fitpath.model.Workout // <-- Your import
import com.example.fitpath.model.WorkoutExercise // <-- Your import

class SeedDataHelper {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun seedExercises(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: "system"
            val userName = auth.currentUser?.displayName ?: "System"

            val sampleExercises = listOf(
                Exercise(
                    name = "Push-ups",
                    description = "Classic chest and tricep exercise",
                    category = "Strength",
                    muscleGroups = listOf("Chest", "Triceps", "Shoulders"),
                    difficulty = "Beginner",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Start in plank position with hands shoulder-width apart",
                        "Lower your body until chest nearly touches the floor",
                        "Push back up to starting position",
                        "Keep core engaged throughout"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Squats",
                    description = "Fundamental lower body exercise",
                    category = "Strength",
                    muscleGroups = listOf("Quadriceps", "Glutes", "Hamstrings"),
                    difficulty = "Beginner",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Stand with feet shoulder-width apart",
                        "Lower your hips back and down",
                        "Keep chest up and knees tracking over toes",
                        "Push through heels to return to standing"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Plank",
                    description = "Core strengthening hold",
                    category = "Strength",
                    muscleGroups = listOf("Core", "Abs", "Back"),
                    difficulty = "Beginner",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Start in push-up position",
                        "Lower onto forearms",
                        "Keep body in straight line from head to heels",
                        "Hold position, engaging core"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Burpees",
                    description = "Full body cardio exercise",
                    category = "Cardio",
                    muscleGroups = listOf("Full Body"),
                    difficulty = "Intermediate",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Start standing",
                        "Drop into squat and place hands on floor",
                        "Jump feet back into plank position",
                        "Do a push-up",
                        "Jump feet back to squat",
                        "Jump up explosively"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Dumbbell Bench Press",
                    description = "Upper body strength builder",
                    category = "Strength",
                    muscleGroups = listOf("Chest", "Triceps", "Shoulders"),
                    difficulty = "Intermediate",
                    equipment = listOf("Dumbbells", "Bench"),
                    instructions = listOf(
                        "Lie on bench with dumbbells at chest level",
                        "Press dumbbells up until arms are extended",
                        "Lower with control back to chest level",
                        "Keep shoulder blades retracted"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Deadlifts",
                    description = "Compound posterior chain exercise",
                    category = "Strength",
                    muscleGroups = listOf("Back", "Glutes", "Hamstrings"),
                    difficulty = "Advanced",
                    equipment = listOf("Barbell"),
                    instructions = listOf(
                        "Stand with feet hip-width apart, bar over mid-foot",
                        "Bend at hips and knees to grip bar",
                        "Keep back straight, chest up",
                        "Drive through heels to lift bar",
                        "Stand fully upright, then lower with control"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Pull-ups",
                    description = "Upper body pulling exercise",
                    category = "Strength",
                    muscleGroups = listOf("Back", "Biceps", "Shoulders"),
                    difficulty = "Intermediate",
                    equipment = listOf("Pull-up Bar"),
                    instructions = listOf(
                        "Hang from bar with palms facing away",
                        "Pull yourself up until chin is over bar",
                        "Lower with control to full extension",
                        "Keep core engaged"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Lunges",
                    description = "Single-leg lower body exercise",
                    category = "Strength",
                    muscleGroups = listOf("Quadriceps", "Glutes", "Hamstrings"),
                    difficulty = "Beginner",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Step forward with one leg",
                        "Lower hips until both knees are at 90 degrees",
                        "Push through front heel to return to start",
                        "Alternate legs"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Mountain Climbers",
                    description = "Dynamic cardio and core exercise",
                    category = "Cardio",
                    muscleGroups = listOf("Core", "Shoulders", "Legs"),
                    difficulty = "Beginner",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Start in plank position",
                        "Drive one knee toward chest",
                        "Quickly switch legs",
                        "Continue alternating at fast pace"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Russian Twists",
                    description = "Rotational core exercise",
                    category = "Strength",
                    muscleGroups = listOf("Obliques", "Core"),
                    difficulty = "Intermediate",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Sit on floor with knees bent, feet elevated",
                        "Lean back slightly, keeping back straight",
                        "Rotate torso side to side",
                        "Touch floor beside hip with each rotation"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Jumping Jacks",
                    description = "Classic cardio warmup",
                    category = "Cardio",
                    muscleGroups = listOf("Full Body"),
                    difficulty = "Beginner",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Stand with feet together, arms at sides",
                        "Jump feet apart while raising arms overhead",
                        "Jump back to starting position",
                        "Maintain steady rhythm"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                ),
                Exercise(
                    name = "Bicycle Crunches",
                    description = "Dynamic ab exercise",
                    category = "Strength",
                    muscleGroups = listOf("Abs", "Obliques"),
                    difficulty = "Beginner",
                    equipment = listOf("None"),
                    instructions = listOf(
                        "Lie on back with hands behind head",
                        "Lift shoulders off ground",
                        "Bring opposite elbow to opposite knee",
                        "Alternate sides in pedaling motion"
                    ),
                    createdBy = userId,
                    createdByName = userName,
                    isPublic = true
                )
            )

            // Add each exercise to Firestore
            sampleExercises.forEach { exercise ->
                db.collection("exercises")
                    .add(exercise)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- This is your new function that their version was missing ---
    suspend fun seedWorkouts(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: "system"
            val userName = auth.currentUser?.displayName ?: "System"

            // 1. Get a few exercises to put in the workout
            val exercises = db.collection("exercises")
                .whereEqualTo("category", "Strength")
                .limit(3) // Get 3 strength exercises
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Exercise::class.java)?.copy(id = it.id) }

            if (exercises.isEmpty()) {
                return Result.failure(Exception("No exercises found. Run seedExercises() first."))
            }

            // 2. Create the list of WorkoutExercise objects
            val workoutExercises = exercises.map { exercise ->
                WorkoutExercise(
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    sets = 3,
                    reps = 10,
                    restTime = 60
                )
            }

            // 3. Create the new Workout object
            val sampleWorkout = Workout(
                name = "Beginner Strength",
                description = "A great starting workout for building muscle.",
                exercises = workoutExercises,
                category = "Strength",
                difficulty = "Beginner",
                durationMinutes = 30,
                createdBy = userId,
                createdByName = userName,
                isPublic = true
            )

            // 4. Save the new workout to the "workouts" collection
            db.collection("workouts").add(sampleWorkout).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}