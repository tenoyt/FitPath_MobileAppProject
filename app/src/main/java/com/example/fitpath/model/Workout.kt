package com.example.fitpath.model

import com.google.firebase.firestore.PropertyName

data class Workout(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val exercises: List<WorkoutExercise> = emptyList(),
    val category: String = "", // e.g., "Full Body", "Upper Body", "Lower Body"
    val difficulty: String = "", // e.g., "Beginner", "Intermediate", "Advanced"
    val durationMinutes: Int = 0,
    val createdBy: String = "", // User ID of creator
    val createdByName: String = "", // Display name of creator

    // Public or private workout
    @get:PropertyName("public")
    @set:PropertyName("public")
    var isPublic: Boolean = true,
    val isFavorite: Boolean = false,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

// Exercise model
data class WorkoutExercise(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val duration: Int = 0, // in seconds, for cardio/timed exercises
    val restTime: Int = 0, // in seconds
    val notes: String = ""
)