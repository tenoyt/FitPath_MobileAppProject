package com.example.fitpath.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitpath.R
import com.example.fitpath.model.Workout
import com.google.android.material.card.MaterialCardView

class WorkoutAdapter(
    private val onWorkoutClick: (Workout) -> Unit,
    private val onFavoriteClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private var workouts = listOf<Workout>()

    fun submitList(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount() = workouts.size

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardWorkout)
        private val textName: TextView = itemView.findViewById(R.id.textWorkoutName)
        private val textDescription: TextView = itemView.findViewById(R.id.textDescription)
        private val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        private val textDifficulty: TextView = itemView.findViewById(R.id.textDifficulty)
        private val textDuration: TextView = itemView.findViewById(R.id.textDuration)
        private val textExerciseCount: TextView = itemView.findViewById(R.id.textExerciseCount)
        private val textCreator: TextView = itemView.findViewById(R.id.textCreator)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(workout: Workout) {
            textName.text = workout.name
            textDescription.text = workout.description
            textCategory.text = workout.category
            textDifficulty.text = workout.difficulty
            textDuration.text = "${workout.durationMinutes} min"
            textExerciseCount.text = "${workout.exercises.size} exercises"
            textCreator.text = "by ${workout.createdByName}"

            // Update favorite icon
            btnFavorite.setImageResource(
                if (workout.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )

            cardView.setOnClickListener {
                onWorkoutClick(workout)
            }

            btnFavorite.setOnClickListener {
                onFavoriteClick(workout)
            }
        }
    }
}