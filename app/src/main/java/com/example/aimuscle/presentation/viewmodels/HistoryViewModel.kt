package com.example.aimuscle.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.WorkoutExercise
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * HistoryViewModel - Load and manage workout sessions/history
 *
 * Responsibilities:
 * - Load all workout sessions
 * - Load sessions for a specific date
 * - Load exercises for a specific session
 * - Delete sessions
 * - Handle loading/error states
 *
 * Usage:
 *   val historyViewModel: HistoryViewModel = viewModel()
 *   val sessions = historyViewModel.allSessions.collectAsState()
 *   historyViewModel.loadSessionsByDate(LocalDate.now())
 *   val sessionExercises = historyViewModel.selectedSessionExercises.collectAsState()
 */
class HistoryViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // UI States
    private val _allSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val allSessions: StateFlow<List<WorkoutSession>> = _allSessions.asStateFlow()

    private val _sessionsByDate = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val sessionsByDate: StateFlow<List<WorkoutSession>> = _sessionsByDate.asStateFlow()

    private val _selectedSessionExercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val selectedSessionExercises: StateFlow<List<WorkoutExercise>> = _selectedSessionExercises.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    init {
        loadAllSessions()
    }

    /**
     * Load all workout sessions from repository
     * Updates loading state and error state accordingly
     */
    fun loadAllSessions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val sessions = repository.getAllSessions()
                _allSessions.value = sessions
            } catch (e: Exception) {
                _error.value = "Failed to load sessions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load sessions for a specific date
     * @param date Target date
     */
    fun loadSessionsByDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val sessions = repository.getSessionsByDate(date)
                _sessionsByDate.value = sessions
            } catch (e: Exception) {
                _error.value = "Failed to load sessions for date: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load exercises for a specific session
     * @param sessionId ID of the session
     */
    fun loadSessionExercises(sessionId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val exercises = repository.getExercisesBySession(sessionId)
                _selectedSessionExercises.value = exercises
            } catch (e: Exception) {
                _error.value = "Failed to load session exercises: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a session and all its exercises
     * @param sessionId ID of session to delete
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                _isDeleting.value = true
                _error.value = null
                _deleteSuccess.value = false

                repository.deleteSession(sessionId)
                _deleteSuccess.value = true

                // Reload all sessions after deletion
                loadAllSessions()
                _selectedSessionExercises.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Failed to delete session: ${e.message}"
            } finally {
                _isDeleting.value = false
            }
        }
    }

    /**
     * Clear selected session exercises
     */
    fun clearSelectedSession() {
        _selectedSessionExercises.value = emptyList()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear delete success state
     */
    fun clearDeleteSuccess() {
        _deleteSuccess.value = false
    }

    /**
     * Refresh all sessions (manual reload)
     */
    fun refreshSessions() {
        loadAllSessions()
    }

    /**
     * Get session by ID
     * @param sessionId ID of session to retrieve
     */
    suspend fun getSessionById(sessionId: Long): WorkoutSession? {
        return try {
            repository.getSessionById(sessionId)
        } catch (e: Exception) {
            _error.value = "Failed to get session: ${e.message}"
            null
        }
    }
}
