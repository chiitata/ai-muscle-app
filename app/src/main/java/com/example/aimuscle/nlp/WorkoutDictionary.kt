package com.example.aimuscle.nlp

/**
 * WorkoutDictionary - Contains common exercise names and their variations
 * Provides a comprehensive mapping of user input to canonical exercise names
 */
object WorkoutDictionary {

    // Exercise categories and their known names/variations
    private val exerciseDictionary = mapOf(
        // Chest exercises
        "pushup" to setOf("push up", "push-up", "pushup", "press", "chest press"),
        "benchpress" to setOf("bench press", "bp", "barbell press", "flat press"),
        "inclinepress" to setOf("incline press", "incline bench", "incline barbell"),
        "flye" to setOf("fly", "flye", "chest fly", "dumbbell fly"),

        // Back exercises
        "pullup" to setOf("pull up", "pull-up", "pullup", "chin up", "strict pull"),
        "pulldown" to setOf("pull down", "lat pulldown", "lat pull", "pulldown"),
        "row" to setOf("row", "barbell row", "dumbbell row", "bent row", "bent over row"),
        "deadlift" to setOf("deadlift", "dead lift", "dl", "conventional deadlift"),

        // Shoulder exercises
        "shoulderpress" to setOf("shoulder press", "military press", "ohp", "overhead press"),
        "lateralraise" to setOf("lateral raise", "side raise", "lateral delt raise"),
        "shrug" to setOf("shrug", "shoulder shrug", "barbell shrug", "dumbbell shrug"),

        // Arm exercises
        "bicepscurl" to setOf("bicep curl", "biceps curl", "barbell curl", "dumbbell curl", "curl"),
        "tricepsdip" to setOf("tricep dip", "triceps dip", "dip", "bench dip"),
        "tricepsextension" to setOf("tricep extension", "triceps extension", "overhead extension"),

        // Leg exercises
        "squat" to setOf("squat", "barbell squat", "back squat", "goblet squat"),
        "legpress" to setOf("leg press", "machine press", "leg machine"),
        "legcurl" to setOf("leg curl", "hamstring curl", "machine curl"),
        "legextension" to setOf("leg extension", "quad extension", "machine extension"),
        "lunge" to setOf("lunge", "walking lunge", "forward lunge", "reverse lunge"),
        "calf" to setOf("calf raise", "calf", "standing calf"),

        // Core exercises
        "plank" to setOf("plank", "planks", "wall plank"),
        "crunch" to setOf("crunch", "crunches", "ab crunch", "machine crunch"),
        "situp" to setOf("sit up", "situp", "sit-up", "ab"),
        "hangingleg" to setOf("hanging leg raise", "leg raise", "hanging ab"),

        // Cardio exercises
        "running" to setOf("running", "run", "jogging", "jog", "sprints"),
        "cycling" to setOf("cycling", "bike", "stationary bike", "spin"),
        "rowing" to setOf("rowing machine", "rower", "row machine"),
        "jumping" to setOf("jump", "jumping jacks", "box jump", "jump rope")
    )

    /**
     * Normalize exercise name by converting input to canonical form
     */
    fun normalizeExerciseName(input: String): String? {
        val normalized = input.toLowerCase()
            .trim()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .trim()

        // Direct lookup
        exerciseDictionary[normalized]?.let { return normalized }

        // Check if input matches any variation
        for ((canonical, variations) in exerciseDictionary) {
            if (variations.any { it.replace(Regex("[^a-z0-9]"), "") == normalized }) {
                return canonical
            }
        }

        return null
    }

    /**
     * Get all known variations of an exercise
     */
    fun getVariations(canonicalName: String): Set<String> {
        return exerciseDictionary[canonicalName] ?: emptySet()
    }

    /**
     * Check if a string is a known exercise
     */
    fun isKnownExercise(input: String): Boolean {
        return normalizeExerciseName(input) != null
    }

    /**
     * Get all canonical exercise names
     */
    fun getAllExercises(): Set<String> {
        return exerciseDictionary.keys
    }
}
