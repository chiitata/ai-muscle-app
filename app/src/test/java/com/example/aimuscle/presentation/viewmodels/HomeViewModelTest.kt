package com.example.aimuscle.presentation.viewmodels

import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.repository.WorkoutRepository
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
import java.time.LocalDateTime

class HomeViewModelTest {

    @Mock
    private lateinit var mockRepository: WorkoutRepository

    private lateinit var homeViewModel: HomeViewModel
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
    fun testLoadTemplatesSuccess() = runBlocking {
        // Arrange
        val mockTemplates = listOf(
            ExerciseTemplate(1, "Upper Body", LocalDateTime.now()),
            ExerciseTemplate(2, "Lower Body", LocalDateTime.now())
        )
        whenever(mockRepository.getAllTemplates()).thenReturn(mockTemplates)

        // Act
        homeViewModel = HomeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val templates = homeViewModel.templates.first()
        assert(templates.size == 2)
        assert(templates[0].templateName == "Upper Body")
        assert(homeViewModel.error.first() == null)
        assert(!homeViewModel.isLoading.first())
    }

    @Test
    fun testLoadTemplatesError() = runBlocking {
        // Arrange
        val errorMessage = "Database error"
        whenever(mockRepository.getAllTemplates()).thenThrow(RuntimeException(errorMessage))

        // Act
        homeViewModel = HomeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val error = homeViewModel.error.first()
        assert(error != null)
        assert(error!!.contains(errorMessage))
        assert(!homeViewModel.isLoading.first())
    }

    @Test
    fun testRefreshTemplates() = runBlocking {
        // Arrange
        val mockTemplates = listOf(
            ExerciseTemplate(1, "Full Body", LocalDateTime.now())
        )
        whenever(mockRepository.getAllTemplates()).thenReturn(mockTemplates)

        // Act
        homeViewModel = HomeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        homeViewModel.refreshTemplates()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val templates = homeViewModel.templates.first()
        assert(templates.size == 1)
    }

    @Test
    fun testClearError() = runBlocking {
        // Arrange
        whenever(mockRepository.getAllTemplates()).thenThrow(RuntimeException("Error"))
        homeViewModel = HomeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        homeViewModel.clearError()

        // Assert
        assert(homeViewModel.error.first() == null)
    }
}
