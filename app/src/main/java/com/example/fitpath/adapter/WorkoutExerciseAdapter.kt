package com.example.fitpath.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitpath.R
import com.example.fitpath.model.WorkoutExercise
import com.google.android.material.card.MaterialCardView

class WorkoutExerciseAdapter(
    private val exercises: MutableList<WorkoutExercise>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val showEditDelete: Boolean = true  // Control visibility of edit/delete buttons
) : RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position], position)
    }

    // Add a method to update the list
    override fun getItemCount() = exercises.size

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView? = itemView.findViewById(R.id.cardExercise)
        private val textName: TextView? = itemView.findViewById(R.id.textExerciseName)
        private val textDetails: TextView? = itemView.findViewById(R.id.textDetails)
        private val textNotes: TextView? = itemView.findViewById(R.id.textNotes)
        private val btnEdit: ImageButton? = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton? = itemView.findViewById(R.id.btnDelete)

        // Bind data to the view
        fun bind(exercise: WorkoutExercise, position: Int) {
            textName?.text = "${position + 1}. ${exercise.exerciseName}"

            val detailsText = buildString {
                if (exercise.sets > 0 && exercise.reps > 0) {
                    append("${exercise.sets} sets × ${exercise.reps} reps")
                }
                if (exercise.duration > 0) {
                    if (isNotEmpty()) append(" • ")
                    append("${exercise.duration}s")
                }
                if (exercise.restTime > 0) {
                    if (isNotEmpty()) append(" • ")
                    append("${exercise.restTime}s rest")
                }
            }
            textDetails?.text = detailsText

            if (exercise.notes.isNotEmpty()) {
                textNotes?.visibility = View.VISIBLE
                textNotes?.text = exercise.notes
            } else {
                textNotes?.visibility = View.GONE
            }

            // Show/hide edit and delete buttons based on showEditDelete parameter
            if (showEditDelete) {
                btnEdit?.visibility = View.VISIBLE
                btnDelete?.visibility = View.VISIBLE

                btnEdit?.setOnClickListener {
                    onEditClick(adapterPosition)
                }

                btnDelete?.setOnClickListener {
                    onDeleteClick(adapterPosition)
                }
            } else {
                btnEdit?.visibility = View.GONE
                btnDelete?.visibility = View.GONE
            }
        }
    }
}