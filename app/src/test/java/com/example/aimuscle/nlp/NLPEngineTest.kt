package com.example.aimuscle.nlp

import org.junit.Assert.*
import org.junit.Test

class NLPEngineTest {

    private val nlpEngine = NLPEngine()

    // ======================== WorkoutDictionary Tests ========================

    @Test
    fun testWorkoutDictionary_NormalizeExerciseName() {
        assertEquals("pushup", WorkoutDictionary.normalizeExerciseName("push up"))
        assertEquals("pushup", WorkoutDictionary.normalizeExerciseName("push-up"))
        assertEquals("pushup", WorkoutDictionary.normalizeExerciseName("PUSHUP"))
        assertEquals("benchpress", WorkoutDictionary.normalizeExerciseName("bench press"))
        assertEquals("squat", WorkoutDictionary.normalizeExerciseName("SQUAT"))
    }

    @Test
    fun testWorkoutDictionary_NormalizeExerciseName_UnknownExercise() {
        assertNull(WorkoutDictionary.normalizeExerciseName("unknownexercise"))
        assertNull(WorkoutDictionary.normalizeExerciseName("xyz123"))
    }

    @Test
    fun testWorkoutDictionary_IsKnownExercise() {
        assertTrue(WorkoutDictionary.isKnownExercise("pushup"))
        assertTrue(WorkoutDictionary.isKnownExercise("bench press"))
        assertTrue(WorkoutDictionary.isKnownExercise("squat"))
        assertTrue(WorkoutDictionary.isKnownExercise("deadlift"))
    }

    @Test
    fun testWorkoutDictionary_IsKnownExercise_Unknown() {
        assertFalse(WorkoutDictionary.isKnownExercise("unknown"))
        assertFalse(WorkoutDictionary.isKnownExercise("fake exercise"))
    }

    @Test
    fun testWorkoutDictionary_GetVariations() {
        val variations = WorkoutDictionary.getVariations("pushup")
        assertTrue(variations.contains("push up"))
        assertTrue(variations.contains("push-up"))
        assertTrue(variations.contains("pushup"))
    }

    @Test
    fun testWorkoutDictionary_GetAllExercises() {
        val exercises = WorkoutDictionary.getAllExercises()
        assertTrue(exercises.isNotEmpty())
        assertTrue(exercises.contains("pushup"))
        assertTrue(exercises.contains("squat"))
        assertTrue(exercises.contains("deadlift"))
    }

    // ======================== PatternExtractor Tests ========================

    @Test
    fun testPatternExtractor_ExtractReps_Simple() {
        assertEquals(10, PatternExtractor.extractReps("10 reps"))
        assertEquals(8, PatternExtractor.extractReps("8 rep"))
        assertEquals(5, PatternExtractor.extractReps("5x"))
        assertEquals(12, PatternExtractor.extractReps("12 × pushups"))
    }

    @Test
    fun testPatternExtractor_ExtractReps_Range() {
        assertEquals(8, PatternExtractor.extractReps("8-10 reps"))
        assertEquals(6, PatternExtractor.extractReps("6–8 reps"))
    }

    @Test
    fun testPatternExtractor_ExtractReps_NoMatch() {
        assertNull(PatternExtractor.extractReps("pushups"))
        assertNull(PatternExtractor.extractReps("bench press"))
    }

    @Test
    fun testPatternExtractor_ExtractSets() {
        assertEquals(3, PatternExtractor.extractSets("3 sets"))
        assertEquals(5, PatternExtractor.extractSets("5 set"))
        assertEquals(4, PatternExtractor.extractSets("4x8 reps"))
    }

    @Test
    fun testPatternExtractor_ExtractSets_NoMatch() {
        assertNull(PatternExtractor.extractSets("10 reps"))
        assertNull(PatternExtractor.extractSets("bench press"))
    }

    @Test
    fun testPatternExtractor_ExtractWeight() {
        assertEquals(185.0, PatternExtractor.extractWeight("185 lbs"), 0.01)
        assertEquals(185.0, PatternExtractor.extractWeight("185 lb"), 0.01)
        assertEquals(225.0, PatternExtractor.extractWeight("225 pounds"), 0.01)
        assertEquals(100.0, PatternExtractor.extractWeight("100 kg"), 0.01)
        assertEquals(50.5, PatternExtractor.extractWeight("50.5 kg"), 0.01)
    }

    @Test
    fun testPatternExtractor_ExtractWeight_NoMatch() {
        assertNull(PatternExtractor.extractWeight("pushups"))
        assertNull(PatternExtractor.extractWeight("10 reps"))
    }

    @Test
    fun testPatternExtractor_ExtractExerciseName() {
        assertEquals("pushups", PatternExtractor.extractExerciseName("10 pushups").trim())
        assertEquals("bench press", PatternExtractor.extractExerciseName("3x8 bench press 185 lbs").trim())
        assertEquals("squat", PatternExtractor.extractExerciseName("squat 5x5 225 lbs").trim())
    }

    @Test
    fun testPatternExtractor_ParseWorkoutLine() {
        val result = PatternExtractor.parseWorkoutLine("3x8 bench press 185 lbs")
        assertEquals(8, result.reps)
        assertEquals(3, result.sets)
        assertEquals(185.0, result.weight, 0.01)
        assertNotNull(result.exerciseName)
    }

    @Test
    fun testPatternExtractor_ParseWorkoutLine_SimpleFormat() {
        val result = PatternExtractor.parseWorkoutLine("10 pushups")
        assertEquals(10, result.reps)
        assertEquals(1, result.sets) // Default
        assertNull(result.weight)
        assertEquals("pushups", result.exerciseName?.trim())
    }

    @Test
    fun testPatternExtractor_IdentifyPattern() {
        assertEquals("sets_x_reps", PatternExtractor.identifyPattern("3 sets of 8 reps"))
        assertEquals("reps_only", PatternExtractor.identifyPattern("10 reps"))
        assertEquals("sets_only", PatternExtractor.identifyPattern("3 sets"))
        assertEquals("weight_focused", PatternExtractor.identifyPattern("185 lbs"))
    }

    // ======================== TFLiteModel Tests ========================

    @Test
    fun testTFLiteModel_ConfidenceScore_ExactMatch() {
        val model = TFLiteModel()
        val score = model.getConfidenceScore("pushup", "pushup")
        assertTrue("Score should be high for exact match", score > 0.90f)
    }

    @Test
    fun testTFLiteModel_ConfidenceScore_CloseVariation() {
        val model = TFLiteModel()
        val score = model.getConfidenceScore("pushup", "push up")
        assertTrue("Score should be high for close variation", score > 0.85f)
    }

    @Test
    fun testTFLiteModel_ConfidenceScore_PartialMatch() {
        val model = TFLiteModel()
        val score = model.getConfidenceScore("pushup", "push")
        assertTrue("Score should be moderate for partial match", score > 0.50f)
    }

    @Test
    fun testTFLiteModel_ConfidenceScore_Range() {
        val model = TFLiteModel()
        val score = model.getConfidenceScore("benchpress", "3x8 bench press 185 lbs")
        assertTrue("Score should be between 0 and 1", score in 0.0f..1.0f)
    }

    // ======================== NLPEngine Tests ========================

    @Test
    fun testNLPEngine_ParseSingleExercise_Simple() {
        val result = nlpEngine.parseSingleExercise("10 pushups")
        assertNotNull(result)
        assertEquals(1, result?.exercises?.size)
        assertEquals("pushup", result?.exercises?.get(0)?.name)
        assertEquals(10, result?.exercises?.get(0)?.reps)
        assertTrue("Confidence should be > 0.5", result?.exercises?.get(0)?.confidence!! > 0.5f)
    }

    @Test
    fun testNLPEngine_ParseSingleExercise_Complex() {
        val result = nlpEngine.parseSingleExercise("3x8 bench press at 185 lbs")
        assertNotNull(result)
        assertEquals(1, result?.exercises?.size)
        assertEquals("benchpress", result?.exercises?.get(0)?.name)
        assertEquals(8, result?.exercises?.get(0)?.reps)
        assertEquals(3, result?.exercises?.get(0)?.sets)
        assertEquals(185.0, result?.exercises?.get(0)?.weight, 0.01)
    }

    @Test
    fun testNLPEngine_ParseSingleExercise_UnknownExercise() {
        val result = nlpEngine.parseSingleExercise("10 unknownexercise")
        assertNull("Should return null for unknown exercise", result)
    }

    @Test
    fun testNLPEngine_ParseSingleExercise_NoReps() {
        val result = nlpEngine.parseSingleExercise("bench press 185 lbs")
        assertNotNull(result)
        assertEquals(10, result?.exercises?.get(0)?.reps) // Default
        assertEquals("benchpress", result?.exercises?.get(0)?.name)
    }

    @Test
    fun testNLPEngine_ParseMultipleExercises() {
        val input = """
            10 pushups
            3x8 bench press 185 lbs
            5x5 squat 225 lbs
        """.trimIndent()

        val result = nlpEngine.parseMultipleExercises(input)
        assertNotNull(result)
        assertEquals(3, result?.exercises?.size)
        assertEquals("pushup", result?.exercises?.get(0)?.name)
        assertEquals("benchpress", result?.exercises?.get(1)?.name)
        assertEquals("squat", result?.exercises?.get(2)?.name)
    }

    @Test
    fun testNLPEngine_Parse_SingleLine() {
        val result = nlpEngine.parse("10 pushups")
        assertNotNull(result)
        assertEquals(1, result?.exercises?.size)
        assertEquals("pushup", result?.exercises?.get(0)?.name)
    }

    @Test
    fun testNLPEngine_Parse_MultiLine() {
        val input = """
            10 pushups
            3x8 bench press 185 lbs
        """.trimIndent()

        val result = nlpEngine.parse(input)
        assertNotNull(result)
        assertEquals(2, result?.exercises?.size)
    }

    @Test
    fun testNLPEngine_Parse_Semicolon() {
        val input = "10 pushups; 3x8 bench press 185 lbs"
        val result = nlpEngine.parse(input)
        assertNotNull(result)
        assertTrue("Should parse semicolon-separated exercises", result?.exercises?.size!! > 1)
    }

    @Test
    fun testNLPEngine_Parse_BlankInput() {
        val result = nlpEngine.parse("")
        assertNull("Should return null for blank input", result)

        val result2 = nlpEngine.parse("   ")
        assertNull("Should return null for whitespace-only input", result2)
    }

    @Test
    fun testNLPEngine_CanParse() {
        assertTrue(nlpEngine.canParse("10 pushups"))
        assertTrue(nlpEngine.canParse("3x8 bench press"))
        assertFalse(nlpEngine.canParse("unknownexercise"))
        assertFalse(nlpEngine.canParse(""))
    }

    @Test
    fun testNLPEngine_IdentifyPattern() {
        assertEquals("sets_x_reps", nlpEngine.identifyPattern("3 sets of 8 reps bench press"))
        assertEquals("reps_only", nlpEngine.identifyPattern("10 pushups"))
        assertEquals("weight_focused", nlpEngine.identifyPattern("185 lbs bench press"))
    }

    @Test
    fun testNLPEngine_GetDetailedAnalysis() {
        val input = "3x8 bench press 185 lbs"
        val analysis = nlpEngine.getDetailedAnalysis(input)

        assertEquals(input, analysis.rawInput)
        assertEquals(1, analysis.exerciseCount)
        assertTrue("Average confidence should be > 0.5", analysis.averageConfidence > 0.5f)
        assertEquals("benchpress", analysis.exercises[0].normalizedExerciseName)
    }

    // ======================== Integration Tests ========================

    @Test
    fun testIntegration_CommonFormats() {
        // Format 1: "X reps exercise"
        val result1 = nlpEngine.parse("10 pushups")
        assertNotNull(result1)
        assertEquals(10, result1?.exercises?.get(0)?.reps)

        // Format 2: "X sets of Y reps exercise"
        val result2 = nlpEngine.parse("3 sets of 8 reps bench press")
        assertNotNull(result2)
        assertEquals(3, result2?.exercises?.get(0)?.sets)
        assertEquals(8, result2?.exercises?.get(0)?.reps)

        // Format 3: "XxY exercise Z lbs"
        val result3 = nlpEngine.parse("5x5 squat 225 lbs")
        assertNotNull(result3)
        assertEquals(5, result3?.exercises?.get(0)?.sets)
        assertEquals(5, result3?.exercises?.get(0)?.reps)
        assertEquals(225.0, result3?.exercises?.get(0)?.weight, 0.01)
    }

    @Test
    fun testIntegration_MultipleFormats() {
        val input = """
            10 pushups
            3 sets of 8 reps bench press 185 lbs
            5x5 squat
            12 rep leg curl 90 lbs
        """.trimIndent()

        val result = nlpEngine.parse(input)
        assertNotNull(result)
        assertEquals(4, result?.exercises?.size)

        // Verify each exercise
        assertEquals("pushup", result?.exercises?.get(0)?.name)
        assertEquals("benchpress", result?.exercises?.get(1)?.name)
        assertEquals("squat", result?.exercises?.get(2)?.name)
        assertEquals("legcurl", result?.exercises?.get(3)?.name)
    }

    @Test
    fun testIntegration_CaseInsensitivity() {
        val result1 = nlpEngine.parse("10 PUSHUPS")
        val result2 = nlpEngine.parse("10 PushUps")
        val result3 = nlpEngine.parse("10 pushups")

        assertEquals("pushup", result1?.exercises?.get(0)?.name)
        assertEquals("pushup", result2?.exercises?.get(0)?.name)
        assertEquals("pushup", result3?.exercises?.get(0)?.name)
    }

    @Test
    fun testIntegration_DefaultValues() {
        val result = nlpEngine.parse("bench press")
        assertNotNull(result)
        assertEquals("benchpress", result?.exercises?.get(0)?.name)
        assertEquals(10, result?.exercises?.get(0)?.reps) // Default
        assertEquals(1, result?.exercises?.get(0)?.sets)   // Default
        assertNull(result?.exercises?.get(0)?.weight)
    }

    @Test
    fun testIntegration_ConfidenceScores() {
        val result = nlpEngine.parse("3x8 bench press 185 lbs")
        assertNotNull(result)

        val confidence = result?.exercises?.get(0)?.confidence
        assertTrue("Confidence should be between 0 and 1", confidence!! in 0.0f..1.0f)
        assertTrue("Confidence should be high for known exercise", confidence > 0.70f)
    }

    @Test
    fun testIntegration_EdgeCases() {
        // Extra spaces
        val result1 = nlpEngine.parse("  10   pushups  ")
        assertNotNull(result1)

        // Special characters
        val result2 = nlpEngine.parse("10-pushups")
        assertNotNull(result2)

        // Mixed punctuation
        val result3 = nlpEngine.parse("10, pushups")
        assertNotNull(result3)
    }
}
