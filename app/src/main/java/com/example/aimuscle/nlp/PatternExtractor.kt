package com.example.aimuscle.nlp

/**
 * PatternExtractor - Uses regex patterns to extract numbers and structure from user input
 * Identifies reps, sets, weight, and exercise names in various formats
 */
object PatternExtractor {

    // Regex patterns for common workout formats
    private val patterns = mapOf(
        "reps" to listOf(
            Regex("(\\d+)\\s*(?:reps?|rep|x|×)", RegexOption.IGNORE_CASE),
            Regex("(\\d+)\\s*(?:times)", RegexOption.IGNORE_CASE)
        ),
        "sets" to listOf(
            Regex("(\\d+)\\s*(?:sets?|set)", RegexOption.IGNORE_CASE),
            Regex("x\\s*(\\d+)", RegexOption.IGNORE_CASE)
        ),
        "weight" to listOf(
            Regex("(\\d+(?:\\.\\d+)?)\\s*(?:lbs?|lb|pounds?)", RegexOption.IGNORE_CASE),
            Regex("(\\d+(?:\\.\\d+)?)\\s*(?:kg|kilograms?)", RegexOption.IGNORE_CASE)
        ),
        "range" to listOf(
            Regex("(\\d+)\\s*[-–]\\s*(\\d+)\\s*(?:reps?|rep|x|×)", RegexOption.IGNORE_CASE)
        )
    )

    data class ExtractionResult(
        val reps: Int? = null,
        val sets: Int? = null,
        val weight: Double? = null,
        val exerciseName: String? = null,
        val rawInput: String
    )

    /**
     * Extract reps from text (single number or range)
     */
    fun extractReps(text: String): Int? {
        // Try range pattern first (e.g., "8-10 reps")
        patterns["range"]?.forEach { pattern ->
            val match = pattern.find(text)
            if (match != null) {
                val numbers = match.groupValues.drop(1).map { it.toIntOrNull() }
                if (numbers.size == 2 && numbers.all { it != null }) {
                    // Return lower bound for range
                    return numbers[0]
                }
            }
        }

        // Try single reps pattern
        patterns["reps"]?.forEach { pattern ->
            val match = pattern.find(text)
            match?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { return it }
        }

        return null
    }

    /**
     * Extract sets from text
     */
    fun extractSets(text: String): Int? {
        patterns["sets"]?.forEach { pattern ->
            val match = pattern.find(text)
            match?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { return it }
        }
        return null
    }

    /**
     * Extract weight from text (supports lbs and kg)
     */
    fun extractWeight(text: String): Double? {
        patterns["weight"]?.forEach { pattern ->
            val match = pattern.find(text)
            match?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let { return it }
        }
        return null
    }

    /**
     * Extract exercise name from text by removing numbers and common keywords
     */
    fun extractExerciseName(text: String): String {
        return text
            // Remove common quantity indicators
            .replace(Regex("\\d+\\s*(?:reps?|rep|sets?|set|x|×|lbs?|kg)", RegexOption.IGNORE_CASE), "")
            // Remove common keywords
            .replace(Regex("\\b(?:of|the|a|an)\\b", RegexOption.IGNORE_CASE), "")
            // Clean up whitespace and punctuation
            .replace(Regex("[,;:\\-–]"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Parse a single line of workout text into structured data
     * Handles formats like:
     * - "10 pushups"
     * - "3 sets of 8 reps bench press at 185 lbs"
     * - "squat 5x5 225 lbs"
     * - "pull ups 3x10"
     */
    fun parseWorkoutLine(line: String): ExtractionResult {
        val reps = extractReps(line)
        val sets = extractSets(line)
        val weight = extractWeight(line)
        val exercise = extractExerciseName(line)

        return ExtractionResult(
            reps = reps,
            sets = sets,
            weight = weight,
            exerciseName = exercise.takeIf { it.isNotBlank() },
            rawInput = line
        )
    }

    /**
     * Parse multiple lines of workout text
     */
    fun parseWorkoutLines(text: String): List<ExtractionResult> {
        return text.split(Regex("[\\n\\r]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { parseWorkoutLine(it) }
    }

    /**
     * Identify common workout sentence patterns
     */
    fun identifyPattern(line: String): String {
        return when {
            Regex("\\d+\\s*(?:sets?|set)", RegexOption.IGNORE_CASE).containsMatchIn(line) &&
                    Regex("\\d+\\s*(?:reps?|rep)", RegexOption.IGNORE_CASE).containsMatchIn(line) -> "sets_x_reps"

            Regex("\\d+\\s*(?:reps?|rep)", RegexOption.IGNORE_CASE).containsMatchIn(line) -> "reps_only"

            Regex("\\d+\\s*(?:sets?|set)", RegexOption.IGNORE_CASE).containsMatchIn(line) -> "sets_only"

            Regex("\\d+\\s*(?:lbs?|kg)", RegexOption.IGNORE_CASE).containsMatchIn(line) -> "weight_focused"

            else -> "unknown"
        }
    }
}
