package com.example.aimuscle.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.models.TemplateExercise
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.domain.models.ParsedExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TemplateViewModel - Load and manage exercise templates
 *
 * Responsibilities:
 * - Load all exercise templates
 * - Load exercises for a specific template
 * - Delete templates
 * - Create new templates
 * - Handle loading/error states
 *
 * Usage:
 *   val templateViewModel: TemplateViewModel = viewModel()
 *   val templates = templateViewModel.allTemplates.collectAsState()
 *   templateViewModel.loadTemplateExercises(templateId)
 *   val exercises = templateViewModel.selectedTemplateExercises.collectAsState()
 *   templateViewModel.deleteTemplate(templateId)
 */
class TemplateViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // UI States
    private val _allTemplates = MutableStateFlow<List<ExerciseTemplate>>(emptyList())
    val allTemplates: StateFlow<List<ExerciseTemplate>> = _allTemplates.asStateFlow()

    private val _selectedTemplateExercises = MutableStateFlow<List<TemplateExercise>>(emptyList())
    val selectedTemplateExercises: StateFlow<List<TemplateExercise>> = _selectedTemplateExercises.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    init {
        loadAllTemplates()
    }

    /**
     * Load all templates from repository
     * Updates loading state and error state accordingly
     */
    fun loadAllTemplates() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val templates = repository.getAllTemplates()
                _allTemplates.value = templates
            } catch (e: Exception) {
                _error.value = "Failed to load templates: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load exercises for a specific template
     * @param templateId ID of the template
     */
    fun loadTemplateExercises(templateId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val exercises = repository.getTemplateExercises(templateId)
                _selectedTemplateExercises.value = exercises
            } catch (e: Exception) {
                _error.value = "Failed to load template exercises: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a template and all its exercises
     * @param templateId ID of template to delete
     */
    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            try {
                _isDeleting.value = true
                _error.value = null
                _deleteSuccess.value = false

                repository.deleteTemplate(templateId)
                _deleteSuccess.value = true

                // Reload templates after deletion
                loadAllTemplates()
                _selectedTemplateExercises.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Failed to delete template: ${e.message}"
            } finally {
                _isDeleting.value = false
            }
        }
    }

    /**
     * Create a new template with exercises
     * @param templateName Name of the template
     * @param exercises List of parsed exercises to save in template
     */
    fun createTemplate(templateName: String, exercises: List<ParsedExercise>) {
        if (templateName.isBlank()) {
            _error.value = "Template name cannot be empty"
            return
        }

        if (exercises.isEmpty()) {
            _error.value = "Template must contain at least one exercise"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.saveTemplate(templateName, exercises)

                // Reload templates after creation
                loadAllTemplates()
            } catch (e: Exception) {
                _error.value = "Failed to create template: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear selected template exercises
     */
    fun clearSelectedTemplate() {
        _selectedTemplateExercises.value = emptyList()
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
     * Refresh all templates (manual reload)
     */
    fun refreshTemplates() {
        loadAllTemplates()
    }
}
