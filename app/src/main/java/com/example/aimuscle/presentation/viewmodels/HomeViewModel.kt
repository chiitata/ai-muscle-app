package com.example.aimuscle.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * HomeViewModel - Load and manage exercise templates for home screen
 *
 * Responsibilities:
 * - Load all exercise templates from repository
 * - Emit templates through StateFlow for reactive UI updates
 * - Handle loading/error states
 *
 * Usage:
 *   val homeViewModel: HomeViewModel = viewModel()
 *   val templates = homeViewModel.templates.collectAsState()
 */
class HomeViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // UI States
    private val _templates = MutableStateFlow<List<ExerciseTemplate>>(emptyList())
    val templates: StateFlow<List<ExerciseTemplate>> = _templates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTemplates()
    }

    /**
     * Load all templates from repository
     * Updates loading state and error state accordingly
     */
    fun loadTemplates() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val loadedTemplates = repository.getAllTemplates()
                _templates.value = loadedTemplates
            } catch (e: Exception) {
                _error.value = "Failed to load templates: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh templates (manual reload)
     */
    fun refreshTemplates() {
        loadTemplates()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}
