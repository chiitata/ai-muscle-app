package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "template_exercise",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,
    val order: Int
)
