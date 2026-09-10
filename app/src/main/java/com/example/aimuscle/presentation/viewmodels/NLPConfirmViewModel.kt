package com.example.aimuscle.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.WorkoutExercise
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.domain.models.ParsedWorkout
import com.example.aimuscle.nlp.NLPEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * NLPConfirmViewModel - Parse, update, and save exercises + sessions
 *
 * Responsibilities:
 * - Parse natural language input using NLPEngine
 * - Manage parsed exercises state
 * - Update individual exercises before confirmation
 * - Save exercises and session to repository
 * - Handle errors during parsing and saving
 *
 * Usage:
 *   val confirmViewModel: NLPConfirmViewModel = viewModel()
 *   confirmViewModel.parseInput("10 pushups, 3x8 bench press 185")
 *   val parsed = confirmViewModel.parsedExercises.collectAsState()
 *   confirmViewModel.updateExercise(0, "Bench Press", sets = 4)
 *   confirmViewModel.saveSession("Upper Body", "Monday workout")
 */
class NLPConfirmViewModel(
    private val nlpEngine: NLPEngine,
    private val repository: WorkoutRepository
) : ViewModel() {

    // UI States
    private val _parsedExercises = MutableStateFlow<List<ParsedExercise>>(emptyList())
    val parsedExercises: StateFlow<List<ParsedExercise>> = _parsedExercises.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val _parseError = MutableStateFlow<String?>(null)
    val parseError: StateFlow<String?> = _parseError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _lastSavedSessionId = MutableStateFlow<Long?>(null)
    val lastSavedSessionId: StateFlow<Long?> = _lastSavedSessionId.asStateFlow()

    /**
     * Parse input text using NLPEngine
     * Updates parsedExercises state with parsed data
     * @param input Natural language workout description
     */
    fun parseInput(input: String) {
        if (input.isBlank()) {
            _parseError.value = "Input cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                _isParsing.value = true
                _parseError.value = null

                val parsedWorkout = nlpEngine.parse(input)
                if (parsedWorkout != null) {
                    _parsedExercises.value = parsedWorkout.exercises
                } else {
                    _parseError.value = "Could not parse workout. Try a clearer format."
                }
            } catch (e: Exception) {
                _parseError.value = "Parsing error: ${e.message}"
            } finally {
                _isParsing.value = false
            }
        }
    }

    /**
     * Update a specific exercise in the parsed list
     * Allows user to correct or modify parsed exercises before saving
     * @param index Index of exercise to update
     * @param name New exercise name
     * @param sets New number of sets
     * @param reps New number of reps
     * @param weight New weight (optional)
     */
    fun updateExercise(
        index: Int,
        name: String? = null,
        sets: Int? = null,
        reps: Int? = null,
        weight: Double? = null
    ) {
        if (index < 0 || index >= _parsedExercises.value.size) {
            _parseError.value = "Invalid exercise index"
            return
        }

        val exercise = _parsedExercises.value[index]
        val updated = exercise.copy(
            name = name ?: exercise.name,
            sets = sets ?: exercise.sets,
            reps = reps ?: exercise.reps,
            weight = weight ?: exercise.weight
        )

        val updatedList = _parsedExercises.value.toMutableList()
        updatedList[index] = updated
        _parsedExercises.value = updatedList
    }

    /**
     * Remove an exercise from the parsed list
     * @param index Index of exercise to remove
     */
    fun removeExercise(index: Int) {
        if (index < 0 || index >= _parsedExercises.value.size) {
            _parseError.value = "Invalid exercise index"
            return
        }

        _parsedExercises.value = _parsedExercises.value
            .filterIndexed { i, _ -> i != index }
    }

    /**
     * Clear all parsed exercises
     */
    fun clearParsed() {
        _parsedExercises.value = emptyList()
        _parseError.value = null
    }

    /**
     * Save parsed exercises and create session
     * @param sessionName Name of the workout session
     * @param sessionNotes Optional notes about the session
     * @param date Date of the session (defaults to today)
     */
    fun saveSession(
        sessionName: String,
        sessionNotes: String? = null,
        date: LocalDate = LocalDate.now()
    ) {
        if (_parsedExercises.value.isEmpty()) {
            _saveError.value = "No exercises to save"
            return
        }

        if (sessionName.isBlank()) {
            _saveError.value = "Session name cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                _isSaving.value = true
                _saveError.value = null
                _saveSuccess.value = false

                // Save session
                val session = WorkoutSession(
                    date = date,
                    menuName = sessionName,
                    notes = sessionNotes,
                    createdAt = LocalDateTime.now()
                )
                val sessionId = repository.saveSession(session)

                // Save exercises
                val exercises = _parsedExercises.value.mapIndexed { index, parsedEx ->
                    WorkoutExercise(
                        sessionId = sessionId,
                        name = parsedEx.name,
                        sets = parsedEx.sets,
                        reps = parsedEx.reps,
                        weight = parsedEx.weight,
                        order = index
                    )
                }
                repository.saveExercises(exercises)

                _lastSavedSessionId.value = sessionId
                _saveSuccess.value = true

                // Clear parsed exercises after successful save
                _parsedExercises.value = emptyList()
            } catch (e: Exception) {
                _saveError.value = "Failed to save session: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Clear error states
     */
    fun clearErrors() {
        _parseError.value = null
        _saveError.value = null
    }

    /**
     * Clear success state
     */
    fun clearSuccess() {
        _saveSuccess.value = false
    }

    /**
     * Get parsing analysis for debugging
     * @param input Text to analyze
     */
    fun getParsingAnalysis(input: String): NLPEngine.ParsingAnalysis {
        return nlpEngine.getDetailedAnalysis(input)
    }
}
