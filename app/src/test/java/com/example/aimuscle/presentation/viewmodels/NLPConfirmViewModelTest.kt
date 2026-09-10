package com.example.aimuscle.presentation.viewmodels

import com.example.aimuscle.data.models.WorkoutExercise
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.domain.models.ParsedWorkout
import com.example.aimuscle.nlp.NLPEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.time.LocalDate

class NLPConfirmViewModelTest {

    @Mock
    private lateinit var mockNLPEngine: NLPEngine

    @Mock
    private lateinit var mockRepository: WorkoutRepository

    private lateinit var confirmViewModel: NLPConfirmViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        confirmViewModel = NLPConfirmViewModel(mockNLPEngine, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testParseInputSuccess() = runBlocking {
        // Arrange
        val input = "10 pushups"
        val parsedExercise = ParsedExercise("Push-ups", 10, 1, null, 0.95f)
        val parsedWorkout = ParsedWorkout(listOf(parsedExercise))
        whenever(mockNLPEngine.parse(input)).thenReturn(parsedWorkout)

        // Act
        confirmViewModel.parseInput(input)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val exercises = confirmViewModel.parsedExercises.first()
        assert(exercises.size == 1)
        assert(exercises[0].name == "Push-ups")
        assert(exercises[0].reps == 10)
        assert(confirmViewModel.parseError.first() == null)
    }

    @Test
    fun testParseInputEmptyString() = runBlocking {
        // Act
        confirmViewModel.parseInput("")

        // Assert
        assert(confirmViewModel.parseError.first()?.contains("empty") == true)
    }

    @Test
    fun testParseInputError() = runBlocking {
        // Arrange
        val input = "invalid input"
        whenever(mockNLPEngine.parse(input)).thenReturn(null)

        // Act
        confirmViewModel.parseInput(input)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assert(confirmViewModel.parseError.first() != null)
        assert(confirmViewModel.parsedExercises.first().isEmpty())
    }

    @Test
    fun testUpdateExercise() = runBlocking {
        // Arrange
        val exercises = listOf(
            ParsedExercise("Bench Press", 8, 3, 185.0, 0.9f)
        )
        confirmViewModel.parseInput("3x8 bench press 185")
        whenever(mockNLPEngine.parse("3x8 bench press 185")).thenReturn(ParsedWorkout(exercises))
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        confirmViewModel.updateExercise(0, sets = 4, reps = 10)

        // Assert
        val updated = confirmViewModel.parsedExercises.first()
        assert(updated[0].sets == 4)
        assert(updated[0].reps == 10)
        assert(updated[0].name == "Bench Press")
    }

    @Test
    fun testRemoveExercise() = runBlocking {
        // Arrange
        val exercises = listOf(
            ParsedExercise("Bench Press", 8, 3, 185.0, 0.9f),
            ParsedExercise("Squat", 5, 5, 225.0, 0.85f)
        )
        whenever(mockNLPEngine.parse("test")).thenReturn(ParsedWorkout(exercises))
        confirmViewModel.parseInput("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        confirmViewModel.removeExercise(0)

        // Assert
        val updated = confirmViewModel.parsedExercises.first()
        assert(updated.size == 1)
        assert(updated[0].name == "Squat")
    }

    @Test
    fun testSaveSessionSuccess() = runBlocking {
        // Arrange
        val exercises = listOf(ParsedExercise("Push-ups", 10, 1, null, 0.9f))
        whenever(mockNLPEngine.parse("10 pushups")).thenReturn(ParsedWorkout(exercises))
        whenever(mockRepository.saveSession(org.mockito.kotlin.any())).thenReturn(1L)
        whenever(mockRepository.saveExercises(org.mockito.kotlin.any())).then { }

        confirmViewModel.parseInput("10 pushups")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        confirmViewModel.saveSession("Morning Workout", "Quick session")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assert(confirmViewModel.saveSuccess.first())
        assert(confirmViewModel.lastSavedSessionId.first() == 1L)
        assert(confirmViewModel.parsedExercises.first().isEmpty())
    }

    @Test
    fun testSaveSessionNoExercises() = runBlocking {
        // Act
        confirmViewModel.saveSession("Empty", null)

        // Assert
        assert(confirmViewModel.saveError.first()?.contains("No exercises") == true)
    }

    @Test
    fun testSaveSessionEmptyName() = runBlocking {
        // Arrange
        val exercises = listOf(ParsedExercise("Push-ups", 10, 1, null, 0.9f))
        whenever(mockNLPEngine.parse("10 pushups")).thenReturn(ParsedWorkout(exercises))
        confirmViewModel.parseInput("10 pushups")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        confirmViewModel.saveSession("", null)

        // Assert
        assert(confirmViewModel.saveError.first()?.contains("name") == true)
    }

    @Test
    fun testClearParsed() = runBlocking {
        // Arrange
        val exercises = listOf(ParsedExercise("Push-ups", 10, 1, null, 0.9f))
        whenever(mockNLPEngine.parse("10 pushups")).thenReturn(ParsedWorkout(exercises))
        confirmViewModel.parseInput("10 pushups")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        confirmViewModel.clearParsed()

        // Assert
        assert(confirmViewModel.parsedExercises.first().isEmpty())
        assert(confirmViewModel.parseError.first() == null)
    }
}
