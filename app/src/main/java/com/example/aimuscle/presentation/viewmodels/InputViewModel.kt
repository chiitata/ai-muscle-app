package com.example.aimuscle.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * InputViewModel - Manage input text state
 *
 * Responsibilities:
 * - Store and update user input text
 * - Validate input
 * - Provide input state through StateFlow for reactive UI
 * - Clear input when needed
 *
 * Usage:
 *   val inputViewModel: InputViewModel = viewModel()
 *   val text = inputViewModel.inputText.collectAsState()
 *   inputViewModel.updateInputText("10 pushups")
 */
class InputViewModel : ViewModel() {

    // UI States
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isInputValid = MutableStateFlow(false)
    val isInputValid: StateFlow<Boolean> = _isInputValid.asStateFlow()

    /**
     * Update input text and validate
     * @param text New input text
     */
    fun updateInputText(text: String) {
        _inputText.value = text
        _isInputValid.value = text.isNotBlank()
    }

    /**
     * Clear input text
     */
    fun clearInput() {
        _inputText.value = ""
        _isInputValid.value = false
    }

    /**
     * Get current input text
     */
    fun getInputText(): String = _inputText.value

    /**
     * Check if input is valid (non-empty)
     */
    fun isValid(): Boolean = _isInputValid.value
}
