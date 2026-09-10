package com.example.aimuscle.presentation.viewmodels

import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.models.TemplateExercise
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.domain.models.ParsedExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.time.LocalDateTime

class TemplateViewModelTest {

    @Mock
    private lateinit var mockRepository: WorkoutRepository

    private lateinit var templateViewModel: TemplateViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadAllTemplatesSuccess() = runBlocking {
        // Arrange
        val mockTemplates = listOf(
            ExerciseTemplate(1, "Upper Body", LocalDateTime.now()),
            ExerciseTemplate(2, "Lower Body", LocalDateTime.now())
        )
        whenever(mockRepository.getAllTemplates()).thenReturn(mockTemplates)

        // Act
        templateViewModel = TemplateViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val templates = templateViewModel.allTemplates.first()
        assert(templates.size == 2)
        assert(!templateViewModel.isLoading.first())
    }

    @Test
    fun testLoadTemplateExercises() = runBlocking {
        // Arrange
        val mockTemplates = listOf(ExerciseTemplate(1, "Upper", LocalDateTime.now()))
        val mockExercises = listOf(
            TemplateExercise(1, 1, "Bench Press", 3, 8, 185.0, 0),
            TemplateExercise(2, 1, "Incline Press", 3, 10, 155.0, 1)
        )
        whenever(mockRepository.getAllTemplates()).thenReturn(mockTemplates)
        whenever(mockRepository.getTemplateExercises(1)).thenReturn(mockExercises)

        templateViewModel = TemplateViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        templateViewModel.loadTemplateExercises(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val exercises = templateViewModel.selectedTemplateExercises.first()
        assert(exercises.size == 2)
        assert(exercises[0].name == "Bench Press")
    }

    @Test
    fun testDeleteTemplateSuccess() = runBlocking {
        // Arrange
        val mockTemplates = listOf(
            ExerciseTemplate(1, "Upper Body", LocalDateTime.now()),
            ExerciseTemplate(2, "Lower Body", LocalDateTime.now())
        )
        whenever(mockRepository.getAllTemplates()).thenReturn(mockTemplates)
        whenever(mockRepository.deleteTemplate(1)).then { }

        templateViewModel = TemplateViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        templateViewModel.deleteTemplate(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assert(templateViewModel.deleteSuccess.first())
        verify(mockRepository).deleteTemplate(1)
    }

    @Test
    fun testCreateTemplateSuccess() = runBlocking {
        // Arrange
        val exercises = listOf(
            ParsedExercise("Bench Press", 8, 3, 185.0, 0.9f),
            ParsedExercise("Squat", 5, 5, 225.0, 0.85f)
        )
        whenever(mockRepository.getAllTemplates()).thenReturn(emptyList())
        whenever(mockRepository.saveTemplate("Upper Body", exercises)).thenReturn(1L)

        templateViewModel = TemplateViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        templateViewModel.createTemplate("Upper Body", exercises)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assert(!templateViewModel.isLoading.first())
        verify(mockRepository).saveTemplate("Upper Body", exercises)
    }

    @Test
    fun testCreateTemplateEmptyName() = runBlocking {
        // Arrange
        templateViewModel = TemplateViewModel(mockRepository)

        // Act
        templateViewModel.createTemplate("", listOf(ParsedExercise("Test", 5, 3, null, 0.9f)))

        // Assert
        assert(templateViewModel.error.first()?.contains("name") == true)
    }

    @Test
    fun testCreateTemplateNoExercises() = runBlocking {
        // Arrange
        templateViewModel = TemplateViewModel(mockRepository)

        // Act
        templateViewModel.createTemplate("Template", emptyList())

        // Assert
        assert(templateViewModel.error.first()?.contains("at least one") == true)
    }

    @Test
    fun testClearSelectedTemplate() = runBlocking {
        // Arrange
        val mockExercises = listOf(TemplateExercise(1, 1, "Bench", 3, 8, 185.0, 0))
        whenever(mockRepository.getAllTemplates()).thenReturn(emptyList())
        whenever(mockRepository.getTemplateExercises(1)).thenReturn(mockExercises)

        templateViewModel = TemplateViewModel(mockRepository)
        templateViewModel.loadTemplateExercises(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        templateViewModel.clearSelectedTemplate()

        // Assert
        assert(templateViewModel.selectedTemplateExercises.first().isEmpty())
    }

    @Test
    fun testClearError() = runBlocking {
        // Arrange
        whenever(mockRepository.getAllTemplates()).thenThrow(RuntimeException("Error"))
        templateViewModel = TemplateViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        templateViewModel.clearError()

        // Assert
        assert(templateViewModel.error.first() == null)
    }
}
