package com.example.fitpath.model

data class Exercise(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "", // e.g., "Strength", "Cardio", "Flexibility"
    val muscleGroups: List<String> = emptyList(), // e.g., "Chest", "Triceps"
    val difficulty: String = "", // "Beginner", "Intermediate", "Advanced"
    val equipment: List<String> = emptyList(), // e.g., "Dumbbells", "Bench"
    val instructions: List<String> = emptyList(),
    val videoUrl: String = "",
    val imageUrl: String = "",
    val createdBy: String = "", // User ID of creator
    val createdByName: String = "", // Display name of creator
    val isPublic: Boolean = true, // Whether it's visible to other users
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)