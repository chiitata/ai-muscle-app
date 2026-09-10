package com.example.aimuscle.nlp

import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.domain.models.ParsedWorkout

/**
 * NLPEngine - Main NLP parser combining rule-based extraction with TensorFlow Lite confidence scoring
 *
 * Processes user input and converts it to structured ParsedWorkout objects
 * Supports multiple input formats:
 * - "10 pushups"
 * - "3 sets of 8 reps bench press at 185 lbs"
 * - "squat 5x5 225"
 * - Multi-line workout descriptions
 */
class NLPEngine(tfliteModelPath: String? = null) {

    private val tfliteModel = TFLiteModel(tfliteModelPath)

    /**
     * Parse a single workout line into a ParsedWorkout
     * Example inputs:
     * - "10 pushups"
     * - "3x8 bench press 185 lbs"
     * - "5 sets of 5 reps squat"
     */
    fun parseSingleExercise(input: String): ParsedWorkout? {
        return try {
            val extracted = PatternExtractor.parseWorkoutLine(input)

            val exerciseName = extracted.exerciseName?.let { name ->
                WorkoutDictionary.normalizeExerciseName(name)
            } ?: return null

            val parsedExercise = ParsedExercise(
                name = exerciseName,
                reps = extracted.reps ?: 10, // Default to 10 reps if not specified
                sets = extracted.sets ?: 1,  // Default to 1 set if not specified
                weight = extracted.weight,
                confidence = tfliteModel.getConfidenceScore(exerciseName, input)
            )

            ParsedWorkout(exercises = listOf(parsedExercise))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse multiple workout lines into a ParsedWorkout
     * Each line is treated as a separate exercise
     * Example input:
     * """
     * 10 pushups
     * 3x8 bench press 185 lbs
     * 5 sets of 5 reps squat
     * """
     */
    fun parseMultipleExercises(input: String): ParsedWorkout? {
        return try {
            val lines = PatternExtractor.parseWorkoutLines(input)
            val exercises = mutableListOf<ParsedExercise>()

            for (extracted in lines) {
                val exerciseName = extracted.exerciseName?.let { name ->
                    WorkoutDictionary.normalizeExerciseName(name)
                } ?: continue

                exercises.add(
                    ParsedExercise(
                        name = exerciseName,
                        reps = extracted.reps ?: 10,
                        sets = extracted.sets ?: 1,
                        weight = extracted.weight,
                        confidence = tfliteModel.getConfidenceScore(exerciseName, extracted.rawInput)
                    )
                )
            }

            if (exercises.isEmpty()) return null
            ParsedWorkout(exercises = exercises)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse workout text - automatically determines if single or multiple exercises
     * This is the main entry point for parsing user input
     */
    fun parse(input: String): ParsedWorkout? {
        if (input.isBlank()) return null

        val trimmedInput = input.trim()

        // Heuristic: if input contains multiple lines or many sentences, treat as multi-exercise
        val isMultiline = trimmedInput.contains('\n') || trimmedInput.contains(';')
        val lineCount = trimmedInput.split(Regex("[\\n;]")).count { it.isNotBlank() }

        return if (isMultiline && lineCount > 1) {
            parseMultipleExercises(trimmedInput)
        } else {
            parseSingleExercise(trimmedInput)
        }
    }

    /**
     * Get the pattern type for a given input (for debugging/analysis)
     */
    fun identifyPattern(input: String): String {
        return PatternExtractor.identifyPattern(input)
    }

    /**
     * Check if an input can be parsed
     */
    fun canParse(input: String): Boolean {
        return parse(input) != null
    }

    /**
     * Get detailed parsing information (for debugging)
     */
    fun getDetailedAnalysis(input: String): ParsingAnalysis {
        val lines = PatternExtractor.parseWorkoutLines(input)
        val exercises = mutableListOf<ExerciseAnalysis>()

        for (extracted in lines) {
            val exerciseName = extracted.exerciseName?.let { name ->
                WorkoutDictionary.normalizeExerciseName(name)
            }

            exercises.add(
                ExerciseAnalysis(
                    rawInput = extracted.rawInput,
                    extractedExerciseName = extracted.exerciseName,
                    normalizedExerciseName = exerciseName,
                    extractedReps = extracted.reps,
                    extractedSets = extracted.sets,
                    extractedWeight = extracted.weight,
                    patternType = PatternExtractor.identifyPattern(extracted.rawInput),
                    confidence = if (exerciseName != null) {
                        tfliteModel.getConfidenceScore(exerciseName, extracted.rawInput)
                    } else {
                        0.0f
                    }
                )
            )
        }

        return ParsingAnalysis(
            rawInput = input,
            exercises = exercises,
            exerciseCount = exercises.size,
            averageConfidence = if (exercises.isNotEmpty()) {
                exercises.mapNotNull { it.confidence }.average().toFloat()
            } else {
                0.0f
            }
        )
    }

    /**
     * Close resources
     */
    fun close() {
        tfliteModel.close()
    }

    // Analysis data classes for debugging
    data class ExerciseAnalysis(
        val rawInput: String,
        val extractedExerciseName: String?,
        val normalizedExerciseName: String?,
        val extractedReps: Int?,
        val extractedSets: Int?,
        val extractedWeight: Double?,
        val patternType: String,
        val confidence: Float
    )

    data class ParsingAnalysis(
        val rawInput: String,
        val exercises: List<ExerciseAnalysis>,
        val exerciseCount: Int,
        val averageConfidence: Float
    )
}
