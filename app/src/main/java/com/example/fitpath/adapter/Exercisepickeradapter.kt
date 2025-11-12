package com.example.fitpath.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitpath.R
import com.example.fitpath.model.Exercise
import com.google.android.material.card.MaterialCardView

class ExercisePickerAdapter(
    private val allExercises: List<Exercise>,
    private val onExerciseClick: (Exercise) -> Unit
) : RecyclerView.Adapter<ExercisePickerAdapter.ExerciseViewHolder>() {

    private var filteredExercises = allExercises

    fun filter(query: String) {
        filteredExercises = if (query.isEmpty()) {
            allExercises
        } else {
            allExercises.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.muscleGroups.any { muscle -> muscle.contains(query, ignoreCase = true) }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_picker, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(filteredExercises[position])
    }

    override fun getItemCount() = filteredExercises.size

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardExercise)
        private val textName: TextView = itemView.findViewById(R.id.textExerciseName)
        private val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        private val textMuscles: TextView = itemView.findViewById(R.id.textMuscles)

        fun bind(exercise: Exercise) {
            textName.text = exercise.name
            textCategory.text = exercise.category
            textMuscles.text = exercise.muscleGroups.joinToString(", ")

            cardView.setOnClickListener {
                onExerciseClick(exercise)
            }
        }
    }
}