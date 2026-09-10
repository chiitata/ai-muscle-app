package com.example.aimuscle.presentation.viewmodels

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class InputViewModelTest {

    private lateinit var inputViewModel: InputViewModel

    @Before
    fun setUp() {
        inputViewModel = InputViewModel()
    }

    @Test
    fun testUpdateInputText() = runBlocking {
        // Act
        inputViewModel.updateInputText("10 pushups")

        // Assert
        assert(inputViewModel.inputText.first() == "10 pushups")
        assert(inputViewModel.isInputValid.first())
    }

    @Test
    fun testClearInput() = runBlocking {
        // Arrange
        inputViewModel.updateInputText("some text")

        // Act
        inputViewModel.clearInput()

        // Assert
        assert(inputViewModel.inputText.first() == "")
        assert(!inputViewModel.isInputValid.first())
    }

    @Test
    fun testIsInputValidWithBlankText() = runBlocking {
        // Act
        inputViewModel.updateInputText("   ")

        // Assert
        assert(!inputViewModel.isInputValid.first())
    }

    @Test
    fun testGetInputText() {
        // Arrange
        inputViewModel.updateInputText("3x8 bench press")

        // Act & Assert
        assert(inputViewModel.getInputText() == "3x8 bench press")
    }

    @Test
    fun testIsValid() {
        // Arrange
        inputViewModel.updateInputText("valid input")

        // Act & Assert
        assert(inputViewModel.isValid())
    }

    @Test
    fun testIsValidWithEmptyText() {
        // Arrange
        inputViewModel.updateInputText("")

        // Act & Assert
        assert(!inputViewModel.isValid())
    }
}
