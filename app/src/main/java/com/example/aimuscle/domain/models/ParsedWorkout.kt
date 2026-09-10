package com.example.aimuscle.domain.models

data class ParsedExercise(
    val name: String,
    val reps: Int,
    val sets: Int = 1,
    val weight: Double? = null,
    val confidence: Float = 1f   // NLP confidence score (0-1)
)

data class ParsedWorkout(
    val exercises: List<ParsedExercise>
)
